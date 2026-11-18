package dev.luizhenriquevicari.remir.app.features.profile

import dev.luizhenriquevicari.remir.core.profile.MeResource
import dev.luizhenriquevicari.remir.core.profile.ProfileDto
import dev.luizhenriquevicari.remir.core.profile.UpdateProfileRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** HTTP calls of the feature, built on the `core` Resources. Errors surface as `ApiException`. */
class ProfileApi(private val client: HttpClient) {
    suspend fun get(): ProfileDto = client.get(MeResource()).body()

    suspend fun update(request: UpdateProfileRequest): ProfileDto =
        client.put(MeResource()) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
