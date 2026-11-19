package dev.luizhenriquevicari.remir.server.features.legal

import kotlinx.html.FlowContent
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.OL
import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.blockQuote
import kotlinx.html.br
import kotlinx.html.code
import kotlinx.html.em
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.h5
import kotlinx.html.h6
import kotlinx.html.hr
import kotlinx.html.li
import kotlinx.html.ol
import kotlinx.html.p
import kotlinx.html.pre
import kotlinx.html.strong
import kotlinx.html.ul
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

private val parser = MarkdownParser(CommonMarkFlavourDescriptor())

/**
 * Renders Markdown as HTML by walking the parsed tree and emitting kotlinx.html tags.
 * No HTML string is ever concatenated, so text is always escaped; raw HTML in the source is dropped,
 * and links are limited to `http`, `https` and `mailto`.
 */
fun FlowContent.markdown(source: String) {
    val tree = parser.buildMarkdownTreeFromString(source)
    val inlines = InlineRenderer(source)
    BlockRenderer(source, inlines).run { blocks(tree) }
}

private val ALLOWED_LINK_PREFIXES = listOf("https://", "http://", "mailto:")

private fun isSafeLink(url: String) = ALLOWED_LINK_PREFIXES.any { url.startsWith(it, ignoreCase = true) }

private fun ASTNode.childrenWithout(vararg types: IElementType): List<ASTNode> = children.filter { it.type !in types }

/** `<h1>` to `<h6>`, indexed by heading level minus one. */
private val HEADINGS: List<FlowContent.(FlowOrPhrasingContent.() -> Unit) -> Unit> =
    listOf(
        { content -> h1 { content() } },
        { content -> h2 { content() } },
        { content -> h3 { content() } },
        { content -> h4 { content() } },
        { content -> h5 { content() } },
        { content -> h6 { content() } },
    )

private val ATX_HEADINGS =
    listOf(
        MarkdownElementTypes.ATX_1,
        MarkdownElementTypes.ATX_2,
        MarkdownElementTypes.ATX_3,
        MarkdownElementTypes.ATX_4,
        MarkdownElementTypes.ATX_5,
        MarkdownElementTypes.ATX_6,
    )

/** Heading level (1 to 6) of a node type, or null when it is not a heading. */
private fun headingLevel(type: IElementType): Int? =
    when (type) {
        MarkdownElementTypes.SETEXT_1 -> 1
        MarkdownElementTypes.SETEXT_2 -> 2
        else -> ATX_HEADINGS.indexOf(type).takeIf { it >= 0 }?.plus(1)
    }

private class BlockRenderer(
    private val source: String,
    private val inlines: InlineRenderer,
) {
    fun FlowContent.blocks(parent: ASTNode) = parent.children.forEach { block(it) }

    private fun FlowContent.block(node: ASTNode) {
        val level = headingLevel(node.type)
        if (level != null) {
            heading(level, node)
            return
        }
        when (node.type) {
            MarkdownElementTypes.PARAGRAPH -> p { with(inlines) { inlines(node.children) } }
            MarkdownElementTypes.UNORDERED_LIST -> ul { listItems(node).forEach { item(it) } }
            MarkdownElementTypes.ORDERED_LIST -> ol { listItems(node).forEach { item(it) } }
            MarkdownElementTypes.BLOCK_QUOTE ->
                blockQuote { node.childrenWithout(MarkdownTokenTypes.BLOCK_QUOTE).forEach { block(it) } }
            MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK -> pre { code { +codeText(node) } }
            MarkdownTokenTypes.HORIZONTAL_RULE -> hr()
            // Whitespace, EOL and raw HTML blocks are intentionally ignored.
            else -> Unit
        }
    }

    private fun FlowContent.heading(
        level: Int,
        node: ASTNode,
    ) {
        val content =
            node.children.firstOrNull {
                it.type == MarkdownTokenTypes.ATX_CONTENT || it.type == MarkdownTokenTypes.SETEXT_CONTENT
            }
        val inner = content?.children?.dropWhile { it.type == MarkdownTokenTypes.WHITE_SPACE }.orEmpty()
        HEADINGS[level - 1](this) { with(inlines) { inlines(inner) } }
    }

    private fun listItems(list: ASTNode): List<ASTNode> =
        list.children.filter {
            it.type ==
                MarkdownElementTypes.LIST_ITEM
        }

    private fun UL.item(node: ASTNode) = li { listItemContent(node) }

    private fun OL.item(node: ASTNode) = li { listItemContent(node) }

    /** Tight list items hold a paragraph: render its text directly instead of nesting a `<p>`. */
    private fun FlowContent.listItemContent(node: ASTNode) {
        val content = node.childrenWithout(MarkdownTokenTypes.LIST_BULLET, MarkdownTokenTypes.LIST_NUMBER)
        content.forEach {
            when (it.type) {
                MarkdownElementTypes.PARAGRAPH -> with(inlines) { inlines(it.children) }
                else -> block(it)
            }
        }
    }

    private fun codeText(node: ASTNode): String =
        node.children
            .filter {
                it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT ||
                    it.type == MarkdownTokenTypes.CODE_LINE ||
                    it.type == MarkdownTokenTypes.EOL
            }.joinToString("") { it.getTextInNode(source).toString() }
            .trim('\n')
}

private class InlineRenderer(
    private val source: String,
) {
    private fun ASTNode.text(): String = getTextInNode(source).toString()

    fun FlowOrPhrasingContent.inlines(nodes: List<ASTNode>) = nodes.forEach { inline(it) }

    private fun FlowOrPhrasingContent.inline(node: ASTNode) {
        when (node.type) {
            MarkdownElementTypes.EMPH -> em { inlines(node.childrenWithout(MarkdownTokenTypes.EMPH)) }
            MarkdownElementTypes.STRONG -> strong { inlines(node.childrenWithout(MarkdownTokenTypes.EMPH)) }
            MarkdownElementTypes.CODE_SPAN ->
                code { +node.childrenWithout(MarkdownTokenTypes.BACKTICK).joinToString("") { it.text() } }
            MarkdownElementTypes.INLINE_LINK -> inlineLink(node)
            MarkdownElementTypes.AUTOLINK -> autoLink(node)
            MarkdownTokenTypes.HARD_LINE_BREAK -> br()
            MarkdownTokenTypes.EOL -> +" "
            // Raw inline HTML is dropped.
            MarkdownElementTypes.HTML_BLOCK, MarkdownTokenTypes.HTML_TAG -> Unit
            else -> leafOrChildren(node)
        }
    }

    private fun FlowOrPhrasingContent.leafOrChildren(node: ASTNode) {
        if (node.children.isEmpty()) {
            +node.text()
            return
        }
        inlines(node.children)
    }

    private fun FlowOrPhrasingContent.inlineLink(node: ASTNode) {
        val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
        val labelNodes = label?.childrenWithout(MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET).orEmpty()
        val destination =
            node.children
                .firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }
                ?.text()
                .orEmpty()
        if (!isSafeLink(destination)) {
            inlines(labelNodes)
            return
        }
        a(href = destination) {
            rel = "noopener noreferrer"
            inlines(labelNodes)
        }
    }

    private fun FlowOrPhrasingContent.autoLink(node: ASTNode) {
        val url = node.text().removePrefix("<").removeSuffix(">")
        if (!isSafeLink(url)) {
            +url
            return
        }
        a(href = url) {
            rel = "noopener noreferrer"
            +url
        }
    }
}
