package dev.luizhenriquevicari.remir.server.features.site

import dev.luizhenriquevicari.remir.server.support.remirTestApplication
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SiteRoutesTest {
    @Test
    fun `home page opens without a token as html`() =
        remirTestApplication { client ->
            val response = client.get("/")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
        }

    @Test
    fun `home page links the legal pages that Google asks for`() =
        remirTestApplication { client ->
            val body = client.get("/").bodyAsText()

            assertTrue("Remir" in body)
            assertTrue("href=\"/legal/privacy\"" in body)
            assertTrue("href=\"/legal/terms\"" in body)
        }
}
