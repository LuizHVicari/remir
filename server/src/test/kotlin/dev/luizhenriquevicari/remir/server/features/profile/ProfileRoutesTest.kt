package dev.luizhenriquevicari.remir.server.features.profile

import dev.luizhenriquevicari.remir.core.api.ApiError
import dev.luizhenriquevicari.remir.core.profile.ProfileDto
import dev.luizhenriquevicari.remir.core.profile.UpdateProfileRequest
import dev.luizhenriquevicari.remir.server.db.dbQuery
import dev.luizhenriquevicari.remir.server.support.TestJwt
import dev.luizhenriquevicari.remir.server.support.remirTestApplication
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class ProfileRoutesTest {
    private fun newUserId() = "user_test_${Uuid.random().toHexString()}"

    @Test
    fun `GET me without token answers 401`() = remirTestApplication { client ->
        val response = client.get("/me")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("unauthorized", response.body<ApiError>().code)
    }

    @Test
    fun `GET me with a token from another issuer answers 401`() = remirTestApplication { client ->
        val token = TestJwt.token(newUserId(), issuer = "https://other.clerk.accounts.dev")

        assertEquals(HttpStatusCode.Unauthorized, client.get("/me") { bearerAuth(token) }.status)
    }

    @Test
    fun `GET me with an expired token answers 401`() = remirTestApplication { client ->
        val token = TestJwt.token(newUserId(), expiresIn = (-5).minutes)

        assertEquals(HttpStatusCode.Unauthorized, client.get("/me") { bearerAuth(token) }.status)
    }

    @Test
    fun `GET me with a valid token answers 200 and provisions the user`() = remirTestApplication { client ->
        val userId = newUserId()

        val response = client.get("/me") { bearerAuth(TestJwt.token(userId)) }

        assertEquals(HttpStatusCode.OK, response.status)
        val profile = response.body<ProfileDto>()
        assertEquals(userId, profile.id)
        assertNull(profile.displayName)
        // Catches time zone bugs in the timestamp column.
        assertTrue((Clock.System.now() - profile.createdAt).absoluteValue < 1.minutes, "createdAt=${profile.createdAt}")
        val rows = dbQuery { UsersTable.selectAll().where { UsersTable.id eq userId }.count() }
        assertEquals(1L, rows)

        // A second access neither duplicates nor changes the user.
        val again = client.get("/me") { bearerAuth(TestJwt.token(userId)) }.body<ProfileDto>()
        assertEquals(profile, again)
    }

    @Test
    fun `PUT me with a valid display name updates it`() = remirTestApplication { client ->
        val response = client.put("/me") {
            bearerAuth(TestJwt.token(newUserId()))
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest("Luiz Henrique"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Luiz Henrique", response.body<ProfileDto>().displayName)
    }

    @Test
    fun `PUT me with an invalid display name answers 400 with fields`() = remirTestApplication { client ->
        val response = client.put("/me") {
            bearerAuth(TestJwt.token(newUserId()))
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest("L"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = response.body<ApiError>()
        assertEquals("validation_failed", error.code)
        assertTrue("displayName" in error.fields, "fields=${error.fields}")
    }

    @Test
    fun `PUT me with malformed JSON answers 400`() = remirTestApplication { client ->
        val response = client.put("/me") {
            bearerAuth(TestJwt.token(newUserId()))
            contentType(ContentType.Application.Json)
            setBody("{not json")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("bad_request", response.body<ApiError>().code)
    }
}
