package dev.luizhenriquevicari.remir.app.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import dev.luizhenriquevicari.remir.core.sleep.SleepResource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** HTTP calls of the feature, built on the `core` Resources. Errors surface as `ApiException`. */
class SleepApi(
    private val client: HttpClient,
) {
    suspend fun list(): List<SleepRecordDto> = client.get(SleepResource()).body()

    suspend fun create(request: CreateSleepRecordRequest): SleepRecordDto =
        client
            .post(SleepResource()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
}
