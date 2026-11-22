package dev.luizhenriquevicari.remir.app.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SaveSleepGoalRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalResource
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import dev.luizhenriquevicari.remir.core.sleep.SleepResource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** HTTP calls of the feature, built on the `core` Resources. Errors surface as `ApiException`. */
class SleepApi(
    private val client: HttpClient,
) {
    suspend fun list(): List<SleepRecordDto> = client.get(SleepResource()).body()

    /** Fails with a 404 `ApiException` while the user has no goal. */
    suspend fun goal(): SleepGoalDto = client.get(SleepGoalResource()).body()

    suspend fun saveGoal(request: SaveSleepGoalRequest): SleepGoalDto =
        client
            .put(SleepGoalResource()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

    suspend fun create(request: CreateSleepRecordRequest): SleepRecordDto =
        client
            .post(SleepResource()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
}
