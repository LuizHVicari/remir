package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.api.ApiError
import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SaveSleepGoalRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import dev.luizhenriquevicari.remir.server.support.TestJwt
import dev.luizhenriquevicari.remir.server.support.remirTestApplication
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.LocalTime
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
    fun `GET sleep with since returns only the nights that started at or after it`() =
        remirTestApplication { client ->
            val userId = newUserId()
            val day = kotlin.time.Duration.parse("1d")
            val nights = (0 until 3).map { night.shifted(day * it) }
            nights.forEach { client.log(userId, it) }

            val since = nights[1].bedtime
            val response =
                client.get("/sleep?since=$since") { bearerAuth(TestJwt.token(userId)) }.body<List<SleepRecordDto>>()

            assertEquals(listOf(nights[2].bedtime, nights[1].bedtime), response.map { it.bedtime })
        }

    @Test
    fun `GET sleep pages through the nights, newest first, without repeating or skipping one`() =
        remirTestApplication { client ->
            val userId = newUserId()
            val day = kotlin.time.Duration.parse("1d")
            val nights = (0 until 5).map { night.shifted(day * it) }
            nights.forEach { client.log(userId, it) }

            suspend fun page(query: String) =
                client.get("/sleep?$query") { bearerAuth(TestJwt.token(userId)) }.body<List<SleepRecordDto>>()

            val newestFirst = nights.map { it.bedtime }.reversed()
            assertEquals(newestFirst.take(2), page("limit=2&offset=0").map { it.bedtime })
            assertEquals(newestFirst.slice(2..3), page("limit=2&offset=2").map { it.bedtime })
            assertEquals(newestFirst.drop(4), page("limit=2&offset=4").map { it.bedtime })
            assertTrue(page("limit=2&offset=6").isEmpty())
        }

    @Test
    fun `GET sleep pages stay stable when two nights started at the same moment`() =
        remirTestApplication { client ->
            val userId = newUserId()
            repeat(4) { client.log(userId, night) }

            suspend fun page(offset: Int) =
                client
                    .get("/sleep?limit=2&offset=$offset") { bearerAuth(TestJwt.token(userId)) }
                    .body<List<SleepRecordDto>>()

            val ids = (page(0) + page(2)).map { it.id }

            assertEquals(4, ids.toSet().size, "ids=$ids")
        }

    @Test
    fun `GET sleep corrects a limit that is too big and an offset below zero`() =
        remirTestApplication { client ->
            val userId = newUserId()
            client.log(userId, night)

            val response = client.get("/sleep?limit=100000&offset=-5") { bearerAuth(TestJwt.token(userId)) }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(1, response.body<List<SleepRecordDto>>().size)
        }

    @Test
    fun `GET sleep with a malformed since answers 400`() =
        remirTestApplication { client ->
            val response = client.get("/sleep?since=yesterday") { bearerAuth(TestJwt.token(newUserId())) }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    private fun CreateSleepRecordRequest.shifted(by: kotlin.time.Duration) =
        copy(
            bedtime = bedtime + by,
            fellAsleepAt = fellAsleepAt?.plus(by),
            wokeUpAt = wokeUpAt + by,
            gotUpAt = gotUpAt + by,
        )

    private suspend fun HttpClient.saveGoal(
        userId: String,
        request: SaveSleepGoalRequest,
    ): HttpResponse =
        put("/sleep/goal") {
            bearerAuth(TestJwt.token(userId))
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    @Test
    fun `sleep goal without token answers 401`() =
        remirTestApplication { client ->
            assertEquals(HttpStatusCode.Unauthorized, client.get("/sleep/goal").status)
            val put =
                client.put("/sleep/goal") {
                    contentType(ContentType.Application.Json)
                    setBody(SaveSleepGoalRequest(sleepMinutes = 480))
                }
            assertEquals(HttpStatusCode.Unauthorized, put.status)
        }

    @Test
    fun `GET sleep goal answers 404 until one is set`() =
        remirTestApplication { client ->
            val response = client.get("/sleep/goal") { bearerAuth(TestJwt.token(newUserId())) }

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("not_found", response.body<ApiError>().code)
        }

    @Test
    fun `PUT sleep goal saves it and GET returns it`() =
        remirTestApplication { client ->
            val userId = newUserId()
            val goal = SaveSleepGoalRequest(LocalTime.parse("23:00"), LocalTime.parse("07:00"), 480)

            val saved = client.saveGoal(userId, goal)

            assertEquals(HttpStatusCode.OK, saved.status)
            val fetched = client.get("/sleep/goal") { bearerAuth(TestJwt.token(userId)) }.body<SleepGoalDto>()
            assertEquals(goal.bedtime, fetched.bedtime)
            assertEquals(goal.wakeUpTime, fetched.wakeUpTime)
            assertEquals(goal.sleepMinutes, fetched.sleepMinutes)
        }

    @Test
    fun `PUT sleep goal replaces the whole goal, clearing parts left out`() =
        remirTestApplication { client ->
            val userId = newUserId()
            client.saveGoal(userId, SaveSleepGoalRequest(LocalTime.parse("23:00"), LocalTime.parse("07:00"), 480))

            client.saveGoal(userId, SaveSleepGoalRequest(sleepMinutes = 450))

            val fetched = client.get("/sleep/goal") { bearerAuth(TestJwt.token(userId)) }.body<SleepGoalDto>()
            assertNull(fetched.bedtime)
            assertNull(fetched.wakeUpTime)
            assertEquals(450, fetched.sleepMinutes)
        }

    @Test
    fun `PUT sleep goal with every part empty answers 400`() =
        remirTestApplication { client ->
            val response = client.saveGoal(newUserId(), SaveSleepGoalRequest())

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("validation_failed", response.body<ApiError>().code)
        }

    @Test
    fun `PUT sleep goal with an out of range duration answers 400 with fields`() =
        remirTestApplication { client ->
            val response = client.saveGoal(newUserId(), SaveSleepGoalRequest(sleepMinutes = 10))

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue("sleepMinutes" in response.body<ApiError>().fields)
        }

    @Test
    fun `user does not see another user's sleep goal`() =
        remirTestApplication { client ->
            val userA = newUserId()
            client.saveGoal(userA, SaveSleepGoalRequest(sleepMinutes = 480))

            val fromB = client.get("/sleep/goal") { bearerAuth(TestJwt.token(newUserId())) }

            assertEquals(HttpStatusCode.NotFound, fromB.status)
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
