package dev.luizhenriquevicari.remir.server.features.legal

import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class MarkdownHtmlTest {
    private fun render(markdown: String): String = createHTML().div { markdown(markdown) }

    @Test
    fun `renders headings, paragraphs and emphasis`() {
        val html = render("# Title\n\n## Section\n\nSome **bold** and *italic* and `code`.")

        assertContains(html, "<h1>Title</h1>")
        assertContains(html, "<h2>Section</h2>")
        assertContains(html, "<strong>bold</strong>")
        assertContains(html, "<em>italic</em>")
        assertContains(html, "<code>code</code>")
    }

    @Test
    fun `renders bullet and ordered lists`() {
        val html = render("- one\n- two\n\n1. first\n2. second")

        assertContains(html, "<ul>")
        assertContains(html, "<li>one</li>")
        assertContains(html, "<ol>")
        assertContains(html, "<li>second</li>")
    }

    @Test
    fun `renders safe links with rel`() {
        val html = render("See [the site](https://example.com/privacy) or <mailto:a@b.co>.")

        assertContains(html, "href=\"https://example.com/privacy\"")
        assertContains(html, "rel=\"noopener noreferrer\"")
        assertContains(html, ">the site</a>")
    }

    @Test
    fun `drops unsafe link schemes and keeps the label`() {
        val html = render("[click me](javascript:alert(1))")

        assertFalse("javascript:" in html, html)
        assertContains(html, "click me")
    }

    @Test
    fun `never emits raw HTML from the source`() {
        val html = render("Hello <script>alert(1)</script> world\n\n<div onclick=\"x()\">block</div>")

        assertFalse("<script" in html, html)
        assertFalse("onclick" in html, html)
    }

    @Test
    fun `escapes special characters in text`() {
        val html = render("Tom & Jerry < 3")

        assertContains(html, "Tom &amp; Jerry &lt; 3")
    }
}
