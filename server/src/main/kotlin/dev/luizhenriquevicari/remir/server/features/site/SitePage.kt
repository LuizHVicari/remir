package dev.luizhenriquevicari.remir.server.features.site

import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.lang
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.section
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

private const val DESCRIPTION =
    "Remir is a personal life-management app: expenses, church, habits, work, study and fitness in one place."

private const val GITHUB_URL = "https://github.com/LuizHVicari/remir"
private const val CONTACT_EMAIL = "luizhenrique.vicari@gmail.com"

/** Same palette as the app (`designsystem/Color.kt`) and as the legal pages; static, trusted CSS. */
private const val SITE_CSS = """
:root {
  color-scheme: light dark;
  --bg: #fcf9f3; --fg: #1b1c18; --muted: #46483f; --accent: #3b4d00; --on-accent: #ffffff;
  --card: #ffffff; --border: #c7c7bc; --glow: rgba(59, 77, 0, .10);
}
@media (prefers-color-scheme: dark) {
  :root {
    --bg: #131410; --fg: #e5e2dc; --muted: #c7c7bc; --accent: #c5f331; --on-accent: #1b2300;
    --card: #1b1c18; --border: #46483f; --glow: rgba(197, 243, 49, .10);
  }
}
* { box-sizing: border-box; }
body {
  margin: 0; color: var(--fg); font: 16px/1.6 system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
  background: radial-gradient(60rem 28rem at 50% -8rem, var(--glow), transparent 70%), var(--bg);
}
main { max-width: 60rem; margin: 0 auto; padding: 4rem 1.25rem 2rem; }
header { text-align: center; padding: 2rem 0 3rem; }
.badge {
  display: inline-block; padding: .25rem .8rem; border: 1px solid var(--border); border-radius: 999px;
  color: var(--muted); font-size: .85rem; letter-spacing: .02em;
}
h1 { margin: 1.25rem 0 .5rem; font-size: clamp(3rem, 10vw, 5rem); line-height: 1; letter-spacing: -.03em; }
.lead { max-width: 36rem; margin: 0 auto; color: var(--muted); font-size: 1.2rem; }
h2 { margin: 0 0 1.25rem; font-size: 1.5rem; text-align: center; }
section { margin-top: 3rem; }
.grid { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fit, minmax(15rem, 1fr)); }
.card { padding: 1.25rem; background: var(--card); border: 1px solid var(--border); border-radius: 1rem; }
.card h3 { margin: 0 0 .25rem; font-size: 1.05rem; }
.card h3::before {
  content: ""; display: inline-block; width: .6rem; height: .6rem; margin-right: .6rem;
  border-radius: 50%; background: var(--accent);
}
.card p { margin: 0; color: var(--muted); font-size: .95rem; }
.note { max-width: 36rem; margin: 0 auto; text-align: center; color: var(--muted); }
footer { margin-top: 4rem; padding-top: 1.5rem; border-top: 1px solid var(--border); text-align: center; }
nav { display: flex; flex-wrap: wrap; gap: .5rem 1.5rem; justify-content: center; }
a { color: var(--accent); text-underline-offset: .2em; }
footer p { margin: 1rem 0 0; color: var(--muted); font-size: .9rem; }
"""

private class Area(
    val name: String,
    val description: String,
)

private val AREAS =
    listOf(
        Area("Expenses", "Track what comes in and goes out, and see where the month went."),
        Area("Church", "Keep your commitments, readings and community in one place."),
        Area("Habits", "Build routines that stick, one day at a time."),
        Area("Work", "Organize tasks and focus on what matters this week."),
        Area("Study", "Plan what you are learning and keep going."),
        Area("Fitness", "Log your training and watch your progress add up."),
    )

/** Public home page: what Remir is, plus the links Google's consent screen and the stores ask for. */
fun HTML.homePage() {
    lang = "en"
    head {
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        meta(name = "description", content = DESCRIPTION)
        title("Remir")
        style { unsafe { raw(SITE_CSS) } }
    }
    body {
        main {
            header {
                span(classes = "badge") { +"Personal project" }
                h1 { +"Remir" }
                p(classes = "lead") { +DESCRIPTION }
            }
            section {
                h2 { +"Everything in one place" }
                div(classes = "grid") {
                    AREAS.forEach { area ->
                        div(classes = "card") {
                            h3 { +area.name }
                            p { +area.description }
                        }
                    }
                }
            }
            section {
                p(classes = "note") {
                    +"Remir is an Android app with a companion API, built and maintained by Luiz Vicari. "
                    +"You sign in with your email or your Google account."
                }
            }
            footer {
                nav {
                    a(href = "/legal/privacy") { +"Privacy Policy" }
                    a(href = "/legal/terms") { +"Terms of Service" }
                    a(href = GITHUB_URL) { +"Source code" }
                    a(href = "mailto:$CONTACT_EMAIL") { +"Contact" }
                }
                p { +"Made by Luiz Vicari" }
            }
        }
    }
}
