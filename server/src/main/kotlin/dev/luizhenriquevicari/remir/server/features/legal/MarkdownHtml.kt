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
    MarkdownRenderer(source).run { blocks(tree) }
}

private val ALLOWED_LINK_PREFIXES = listOf("https://", "http://", "mailto:")

private fun isSafeLink(url: String) = ALLOWED_LINK_PREFIXES.any { url.startsWith(it, ignoreCase = true) }

private class MarkdownRenderer(private val source: String) {
    private fun ASTNode.text(): String = getTextInNode(source).toString()

    private fun ASTNode.childrenWithout(vararg types: IElementType): List<ASTNode> = children.filter { it.type !in types }

    fun FlowContent.blocks(parent: ASTNode) = parent.children.forEach { block(it) }

    private fun FlowContent.block(node: ASTNode) {
        when (node.type) {
            MarkdownElementTypes.ATX_1, MarkdownElementTypes.SETEXT_1 -> heading(1, node)
            MarkdownElementTypes.ATX_2, MarkdownElementTypes.SETEXT_2 -> heading(2, node)
            MarkdownElementTypes.ATX_3 -> heading(3, node)
            MarkdownElementTypes.ATX_4 -> heading(4, node)
            MarkdownElementTypes.ATX_5 -> heading(5, node)
            MarkdownElementTypes.ATX_6 -> heading(6, node)
            MarkdownElementTypes.PARAGRAPH -> p { inlines(node.children) }
            MarkdownElementTypes.UNORDERED_LIST -> ul { node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }.forEach { item(it) } }
            MarkdownElementTypes.ORDERED_LIST -> ol { node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }.forEach { item(it) } }
            MarkdownElementTypes.BLOCK_QUOTE -> blockQuote {
                node.childrenWithout(MarkdownTokenTypes.BLOCK_QUOTE).forEach { block(it) }
            }
            MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK -> pre { code { +codeText(node) } }
            MarkdownTokenTypes.HORIZONTAL_RULE -> hr()
            // Whitespace, EOL and raw HTML blocks are intentionally ignored.
            else -> Unit
        }
    }

    private fun FlowContent.heading(level: Int, node: ASTNode) {
        val content = node.children.firstOrNull {
            it.type == MarkdownTokenTypes.ATX_CONTENT || it.type == MarkdownTokenTypes.SETEXT_CONTENT
        }
        val inner = content?.children?.dropWhile { it.type == MarkdownTokenTypes.WHITE_SPACE }.orEmpty()
        when (level) {
            1 -> h1 { inlines(inner) }
            2 -> h2 { inlines(inner) }
            3 -> h3 { inlines(inner) }
            4 -> h4 { inlines(inner) }
            5 -> h5 { inlines(inner) }
            else -> h6 { inlines(inner) }
        }
    }

    private fun UL.item(node: ASTNode) = li { listItemContent(node) }

    private fun OL.item(node: ASTNode) = li { listItemContent(node) }

    /** Tight list items hold a paragraph: render its text directly instead of nesting a `<p>`. */
    private fun FlowContent.listItemContent(node: ASTNode) {
        val content = node.childrenWithout(MarkdownTokenTypes.LIST_BULLET, MarkdownTokenTypes.LIST_NUMBER)
        content.forEach {
            when (it.type) {
                MarkdownElementTypes.PARAGRAPH -> inlines(it.children)
                else -> block(it)
            }
        }
    }

    private fun codeText(node: ASTNode): String =
        node.children
            .filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT || it.type == MarkdownTokenTypes.CODE_LINE || it.type == MarkdownTokenTypes.EOL }
            .joinToString("") { it.text() }
            .trim('\n')

    private fun FlowOrPhrasingContent.inlines(nodes: List<ASTNode>) = nodes.forEach { inline(it) }

    private fun FlowOrPhrasingContent.inline(node: ASTNode) {
        when (node.type) {
            MarkdownElementTypes.EMPH -> em { inlines(node.childrenWithout(MarkdownTokenTypes.EMPH)) }
            MarkdownElementTypes.STRONG -> strong { inlines(node.childrenWithout(MarkdownTokenTypes.EMPH)) }
            MarkdownElementTypes.CODE_SPAN -> code { +node.childrenWithout(MarkdownTokenTypes.BACKTICK).joinToString("") { it.text() } }
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
        val destination = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }?.text().orEmpty()
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
