package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.api.ApiError
import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import dev.luizhenriquevicari.remir.server.support.TestJwt
import dev.luizhenriquevicari.remir.server.support.remirTestApplication
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SleepRoutesTest {
    private fun newUserId() = "user_test_${Uuid.random().toHexString()}"

    private val night =
        CreateSleepRecordRequest(
            bedtime = Instant.parse("2026-03-10T23:00:00Z"),
            fellAsleepAt = Instant.parse("2026-03-10T23:30:00Z"),
            wokeUpAt = Instant.parse("2026-03-11T07:00:00Z"),
            gotUpAt = Instant.parse("2026-03-11T07:15:00Z"),
        )

    private suspend fun HttpClient.log(
        userId: String,
        request: CreateSleepRecordRequest,
    ): HttpResponse =
        post("/sleep") {
            bearerAuth(TestJwt.token(userId))
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    private suspend fun HttpClient.records(userId: String): List<SleepRecordDto> =
        get("/sleep") { bearerAuth(TestJwt.token(userId)) }.body()

    @Test
    fun `GET sleep without token answers 401`() =
        remirTestApplication { client ->
            assertEquals(HttpStatusCode.Unauthorized, client.get("/sleep").status)
        }

    @Test
    fun `POST sleep without token answers 401`() =
        remirTestApplication { client ->
            val response =
                client.post("/sleep") {
                    contentType(ContentType.Application.Json)
                    setBody(night)
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST sleep creates a record that shows up in the list`() =
        remirTestApplication { client ->
            val userId = newUserId()

            val response = client.log(userId, night)

            assertEquals(HttpStatusCode.Created, response.status)
            val created = response.body<SleepRecordDto>()
            assertEquals(night.bedtime, created.bedtime)
            assertEquals(night.fellAsleepAt, created.fellAsleepAt)
            assertEquals(night.gotUpAt, created.gotUpAt)
            assertEquals(listOf(created), client.records(userId))
        }

    @Test
    fun `POST sleep accepts a night without the fell asleep time`() =
        remirTestApplication { client ->
            val response = client.log(newUserId(), night.copy(fellAsleepAt = null))

            assertEquals(HttpStatusCode.Created, response.status)
            assertNull(response.body<SleepRecordDto>().fellAsleepAt)
        }

    @Test
    fun `POST sleep with out of order times answers 400 with fields`() =
        remirTestApplication { client ->
            val response = client.log(newUserId(), night.copy(wokeUpAt = night.bedtime))

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val error = response.body<ApiError>()
            assertEquals("validation_failed", error.code)
            assertTrue("wokeUpAt" in error.fields, "fields=${error.fields}")
        }

    @Test
    fun `GET sleep lists the newest night first`() =
        remirTestApplication { client ->
            val userId = newUserId()
            val nextNight =
                CreateSleepRecordRequest(
                    bedtime = night.bedtime + kotlin.time.Duration.parse("1d"),
                    wokeUpAt = night.wokeUpAt + kotlin.time.Duration.parse("1d"),
                    gotUpAt = night.gotUpAt + kotlin.time.Duration.parse("1d"),
                )
            client.log(userId, night)
            client.log(userId, nextNight)

            assertEquals(listOf(nextNight.bedtime, night.bedtime), client.records(userId).map { it.bedtime })
        }

    @Test
    fun `user does not see another user's sleep`() =
        remirTestApplication { client ->
            val userA = newUserId()
            val userB = newUserId()
            client.log(userA, night)

            assertTrue(client.records(userB).isEmpty())
            assertEquals(1, client.records(userA).size)
        }
}
