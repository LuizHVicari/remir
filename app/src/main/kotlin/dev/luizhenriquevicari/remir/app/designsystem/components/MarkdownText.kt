package dev.luizhenriquevicari.remir.app.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

/**
 * Renders Markdown natively (no WebView). Raw HTML in the source is dropped and links are limited to
 * `http`, `https` and `mailto`, the same rules as the server's HTML page.
 */
@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceContainerHigh
    val blocks = remember(markdown, linkColor, codeBackground) {
        MarkdownBlockParser(markdown, linkColor, codeBackground).parse()
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        blocks.forEach { Block(it) }
    }
}

private sealed interface MdBlock {
    data class Heading(val level: Int, val text: AnnotatedString) : MdBlock

    data class Paragraph(val text: AnnotatedString) : MdBlock

    data class Items(val ordered: Boolean, val items: List<List<MdBlock>>) : MdBlock

    data class Quote(val blocks: List<MdBlock>) : MdBlock

    data class Code(val text: String) : MdBlock

    data object Rule : MdBlock
}

@Composable
private fun Block(block: MdBlock) {
    when (block) {
        is MdBlock.Heading -> Text(block.text, style = headingStyle(block.level), modifier = Modifier.padding(top = 8.dp))
        is MdBlock.Paragraph -> Text(block.text, style = MaterialTheme.typography.bodyLarge)
        is MdBlock.Items -> ItemsBlock(block)
        is MdBlock.Quote -> QuoteBlock(block)
        is MdBlock.Code -> Text(
            block.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(12.dp),
        )
        MdBlock.Rule -> HorizontalDivider(Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun headingStyle(level: Int) = when (level) {
    1 -> MaterialTheme.typography.headlineSmall
    2 -> MaterialTheme.typography.titleLarge
    3 -> MaterialTheme.typography.titleMedium
    else -> MaterialTheme.typography.titleSmall
}

@Composable
private fun ItemsBlock(block: MdBlock.Items) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        block.items.forEachIndexed { index, item ->
            Row {
                Text(
                    text = if (block.ordered) "${index + 1}." else "•",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(28.dp),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.forEach { Block(it) }
                }
            }
        }
    }
}

@Composable
private fun QuoteBlock(block: MdBlock.Quote) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Spacer(Modifier.width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant))
        Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            block.blocks.forEach { Block(it) }
        }
    }
}

private val ALLOWED_LINK_PREFIXES = listOf("https://", "http://", "mailto:")

private fun isSafeLink(url: String) = ALLOWED_LINK_PREFIXES.any { url.startsWith(it, ignoreCase = true) }

private class MarkdownBlockParser(
    private val source: String,
    private val linkColor: Color,
    private val codeBackground: Color,
) {
    private val parser = MarkdownParser(CommonMarkFlavourDescriptor())

    fun parse(): List<MdBlock> = blocks(parser.buildMarkdownTreeFromString(source))

    private fun ASTNode.text(): String = getTextInNode(source).toString()

    private fun ASTNode.childrenWithout(vararg types: IElementType): List<ASTNode> = children.filter { it.type !in types }

    private fun blocks(parent: ASTNode): List<MdBlock> = parent.children.mapNotNull { block(it) }

    private fun block(node: ASTNode): MdBlock? = when (node.type) {
        MarkdownElementTypes.ATX_1, MarkdownElementTypes.SETEXT_1 -> heading(1, node)
        MarkdownElementTypes.ATX_2, MarkdownElementTypes.SETEXT_2 -> heading(2, node)
        MarkdownElementTypes.ATX_3 -> heading(3, node)
        MarkdownElementTypes.ATX_4 -> heading(4, node)
        MarkdownElementTypes.ATX_5 -> heading(5, node)
        MarkdownElementTypes.ATX_6 -> heading(6, node)
        MarkdownElementTypes.PARAGRAPH -> MdBlock.Paragraph(inlineText(node.children))
        MarkdownElementTypes.UNORDERED_LIST -> MdBlock.Items(ordered = false, items = listItems(node))
        MarkdownElementTypes.ORDERED_LIST -> MdBlock.Items(ordered = true, items = listItems(node))
        MarkdownElementTypes.BLOCK_QUOTE -> MdBlock.Quote(node.childrenWithout(MarkdownTokenTypes.BLOCK_QUOTE).mapNotNull { block(it) })
        MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK -> MdBlock.Code(codeText(node))
        MarkdownTokenTypes.HORIZONTAL_RULE -> MdBlock.Rule
        // Whitespace, EOL and raw HTML blocks are intentionally ignored.
        else -> null
    }

    private fun heading(level: Int, node: ASTNode): MdBlock {
        val content = node.children.firstOrNull {
            it.type == MarkdownTokenTypes.ATX_CONTENT || it.type == MarkdownTokenTypes.SETEXT_CONTENT
        }
        val inner = content?.children?.dropWhile { it.type == MarkdownTokenTypes.WHITE_SPACE }.orEmpty()
        return MdBlock.Heading(level, inlineText(inner))
    }

    private fun listItems(list: ASTNode): List<List<MdBlock>> =
        list.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }.map { item ->
            item.childrenWithout(MarkdownTokenTypes.LIST_BULLET, MarkdownTokenTypes.LIST_NUMBER).mapNotNull { block(it) }
        }

    private fun codeText(node: ASTNode): String =
        node.children
            .filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT || it.type == MarkdownTokenTypes.CODE_LINE || it.type == MarkdownTokenTypes.EOL }
            .joinToString("") { it.text() }
            .trim('\n')

    private fun inlineText(nodes: List<ASTNode>): AnnotatedString = buildAnnotatedString { inlines(nodes) }

    private fun AnnotatedString.Builder.inlines(nodes: List<ASTNode>) = nodes.forEach { inline(it) }

    private fun AnnotatedString.Builder.inline(node: ASTNode) {
        when (node.type) {
            MarkdownElementTypes.EMPH -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                inlines(node.childrenWithout(MarkdownTokenTypes.EMPH))
            }
            MarkdownElementTypes.STRONG -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                inlines(node.childrenWithout(MarkdownTokenTypes.EMPH))
            }
            MarkdownElementTypes.CODE_SPAN -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground)) {
                append(node.childrenWithout(MarkdownTokenTypes.BACKTICK).joinToString("") { it.text() })
            }
            MarkdownElementTypes.INLINE_LINK -> inlineLink(node)
            MarkdownElementTypes.AUTOLINK -> autoLink(node)
            MarkdownTokenTypes.HARD_LINE_BREAK -> append('\n')
            MarkdownTokenTypes.EOL -> append(' ')
            // Raw inline HTML is dropped.
            MarkdownElementTypes.HTML_BLOCK, MarkdownTokenTypes.HTML_TAG -> Unit
            else -> leafOrChildren(node)
        }
    }

    private fun AnnotatedString.Builder.leafOrChildren(node: ASTNode) {
        if (node.children.isEmpty()) {
            append(node.text())
            return
        }
        inlines(node.children)
    }

    private fun AnnotatedString.Builder.inlineLink(node: ASTNode) {
        val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
        val labelNodes = label?.childrenWithout(MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET).orEmpty()
        val destination = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }?.text().orEmpty()
        if (!isSafeLink(destination)) {
            inlines(labelNodes)
            return
        }
        withLink(linkAnnotation(destination)) { inlines(labelNodes) }
    }

    private fun AnnotatedString.Builder.autoLink(node: ASTNode) {
        val url = node.text().removePrefix("<").removeSuffix(">")
        if (!isSafeLink(url)) {
            append(url)
            return
        }
        withLink(linkAnnotation(url)) { append(url) }
    }

    private fun linkAnnotation(url: String) = LinkAnnotation.Url(
        url,
        TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
    )
}

private const val SAMPLE = """# Privacy Policy

Remir is operated by **Luiz Vicari**. Read the *full* text at [our site](https://example.com) or write to <mailto:me@example.com>.

## Data I process

- Account data, handled by `Clerk`
- The display name you choose

1. First step
2. Second step

> Keep your own copy of anything important.

---

Unsafe [link](javascript:alert(1)) and <script>alert(1)</script> are dropped."""

@PreviewLightDark
@Composable
private fun MarkdownTextPreview() {
    RemirPreview {
        MarkdownText(SAMPLE, Modifier.padding(16.dp))
    }
}
