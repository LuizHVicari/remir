package dev.luizhenriquevicari.remir.server.features.legal

import dev.luizhenriquevicari.remir.core.legal.LegalDocumentDto
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.footer
import kotlinx.html.head
import kotlinx.html.lang
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

/** Same palette as the app (`designsystem/Color.kt`); static, trusted CSS. */
private const val PAGE_CSS = """
:root { color-scheme: light dark; --bg: #fcf9f3; --fg: #1b1c18; --muted: #46483f; --accent: #3b4d00; --border: #c7c7bc; }
@media (prefers-color-scheme: dark) {
  :root { --bg: #131410; --fg: #e5e2dc; --muted: #c7c7bc; --accent: #c5f331; --border: #46483f; }
}
body { margin: 0; background: var(--bg); color: var(--fg); font: 16px/1.6 system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; }
main { max-width: 42rem; margin: 0 auto; padding: 3rem 1.25rem; }
h1, h2, h3 { line-height: 1.2; }
h2 { margin-top: 2rem; }
a { color: var(--accent); }
blockquote { margin-left: 0; padding-left: 1rem; border-left: 3px solid var(--border); color: var(--muted); }
pre { overflow-x: auto; padding: .75rem; border: 1px solid var(--border); }
footer { margin-top: 3rem; padding-top: 1rem; border-top: 1px solid var(--border); color: var(--muted); font-size: .9rem; }
"""

fun LegalDocumentType.displayName(): String = when (this) {
    LegalDocumentType.Privacy -> "Privacy Policy"
    LegalDocumentType.Terms -> "Terms of Service"
}

/** Public page of one document version: the Markdown content plus its version and effective date. */
fun HTML.legalPage(document: LegalDocumentDto) {
    lang = "en"
    head {
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        title("${document.type.displayName()} - Remir")
        style { unsafe { raw(PAGE_CSS) } }
    }
    body {
        main {
            markdown(document.content)
            footer {
                +"Version ${document.version}, effective ${document.effectiveAt.toLocalDateTime(TimeZone.UTC).date}"
            }
        }
    }
}
