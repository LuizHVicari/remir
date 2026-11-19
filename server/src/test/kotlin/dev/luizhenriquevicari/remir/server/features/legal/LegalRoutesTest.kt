package dev.luizhenriquevicari.remir.server.features.legal

import dev.luizhenriquevicari.remir.core.api.ApiError
import dev.luizhenriquevicari.remir.core.legal.AcceptLegalDocumentRequest
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentDto
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentType
import dev.luizhenriquevicari.remir.core.legal.LegalStatusDto
import dev.luizhenriquevicari.remir.core.legal.PublishLegalDocumentRequest
import dev.luizhenriquevicari.remir.server.support.TestJwt
import dev.luizhenriquevicari.remir.server.support.remirTestApplication
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/**
 * Documents are global (not per user) and the database is shared by the whole test JVM, so these tests
 * never assume a version number: they publish, then compare against what came back.
 */
class LegalRoutesTest {
    private fun newUserId() = "user_test_${Uuid.random().toHexString()}"

    private fun adminToken() = TestJwt.token(newUserId(), role = "admin")

    private suspend fun HttpClient.publish(
        type: String,
        content: String,
        requiresAcceptance: Boolean,
        token: String = adminToken(),
    ): HttpResponse =
        post("/admin/legal-documents/$type") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(PublishLegalDocumentRequest(content, requiresAcceptance))
        }

    private suspend fun HttpClient.pending(token: String): List<LegalDocumentDto> =
        get("/me/legal") { bearerAuth(token) }.body<LegalStatusDto>().pending

    private suspend fun HttpClient.accept(
        type: String,
        version: Int,
        token: String,
    ): HttpResponse =
        post("/legal-documents/$type/acceptances") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(AcceptLegalDocumentRequest(version))
        }

    @Test
    fun `publishing without a token answers 401`() =
        remirTestApplication { client ->
            val response =
                client.post("/admin/legal-documents/terms") {
                    contentType(ContentType.Application.Json)
                    setBody(PublishLegalDocumentRequest("# Terms", requiresAcceptance = true))
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `publishing as a regular user answers 403`() =
        remirTestApplication { client ->
            val response = client.publish("terms", "# Terms", true, token = TestJwt.token(newUserId()))

            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals("forbidden", response.body<ApiError>().code)
        }

    @Test
    fun `admin publishes versions that increase and become the current one`() =
        remirTestApplication { client ->
            val first = client.publish("terms", "# Terms v-a", true)
            val second = client.publish("terms", "# Terms v-b", false)

            assertEquals(HttpStatusCode.Created, first.status)
            val firstDoc = first.body<LegalDocumentDto>()
            val secondDoc = second.body<LegalDocumentDto>()
            assertEquals(firstDoc.version + 1, secondDoc.version)
            val current = client.get("/legal-documents/terms") { bearerAuth(adminToken()) }.body<LegalDocumentDto>()
            assertEquals(secondDoc, current)
        }

    @Test
    fun `publishing blank content answers 400 with fields`() =
        remirTestApplication { client ->
            val response = client.publish("terms", "   ", true)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue("content" in response.body<ApiError>().fields)
        }

    @Test
    fun `public page renders the current version without a token and never raw HTML`() =
        remirTestApplication { client ->
            client.publish("privacy", "# Privacy Page Test\n\nHello <script>alert(1)</script>", true)

            val response = client.get("/legal/privacy")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
            val html = response.bodyAsText()
            assertTrue("<h1>Privacy Page Test</h1>" in html, html)
            assertFalse("<script>alert" in html, html)
        }

    @Test
    fun `acceptance flow asks once, again only when a new version requires it`() =
        remirTestApplication { client ->
            val token = TestJwt.token(newUserId())
            val v1 = client.publish("terms", "# Flow v1", true).body<LegalDocumentDto>()

            assertTrue(client.pending(token).any { it.type == LegalDocumentType.Terms && it.version == v1.version })

            assertEquals(HttpStatusCode.NoContent, client.accept("terms", v1.version, token).status)
            assertTrue(client.pending(token).none { it.type == LegalDocumentType.Terms })

            // A typo fix does not ask again.
            client.publish("terms", "# Flow v1 fixed", false)
            assertTrue(client.pending(token).none { it.type == LegalDocumentType.Terms })

            // A material change does.
            val v3 = client.publish("terms", "# Flow v3", true).body<LegalDocumentDto>()
            assertTrue(client.pending(token).any { it.type == LegalDocumentType.Terms && it.version == v3.version })
        }

    @Test
    fun `accepting a stale version answers 409`() =
        remirTestApplication { client ->
            val token = TestJwt.token(newUserId())
            val old = client.publish("terms", "# Stale old", true).body<LegalDocumentDto>()
            client.publish("terms", "# Stale new", true)

            val response = client.accept("terms", old.version, token)

            assertEquals(HttpStatusCode.Conflict, response.status)
            assertEquals("conflict", response.body<ApiError>().code)
        }

    @Test
    fun `one user accepting does not affect another user`() =
        remirTestApplication { client ->
            val userA = TestJwt.token(newUserId())
            val userB = TestJwt.token(newUserId())
            val doc = client.publish("terms", "# Isolation", true).body<LegalDocumentDto>()

            client.accept("terms", doc.version, userA)

            assertTrue(client.pending(userA).none { it.type == LegalDocumentType.Terms })
            assertTrue(client.pending(userB).any { it.type == LegalDocumentType.Terms && it.version == doc.version })
        }

    @Test
    fun `accepting with an invalid version answers 400`() =
        remirTestApplication { client ->
            client.publish("terms", "# Invalid", true)

            val response = client.accept("terms", 0, TestJwt.token(newUserId()))

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue("version" in response.body<ApiError>().fields)
        }
}
