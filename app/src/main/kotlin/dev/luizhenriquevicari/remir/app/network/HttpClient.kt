package dev.luizhenriquevicari.remir.app.network

import dev.luizhenriquevicari.remir.core.api.ApiError
import dev.luizhenriquevicari.remir.core.api.RemirJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.json

fun createHttpClient(
    baseUrl: String,
    tokenProvider: suspend () -> String?,
): HttpClient =
    HttpClient(OkHttp) {
        expectSuccess = true
        install(ContentNegotiation) { json(RemirJson) }
        install(Resources)
        install(ClerkToken) { this.tokenProvider = tokenProvider }
        defaultRequest { url(baseUrl) }
        HttpResponseValidator {
            // Turns every non-2xx response into an ApiException carrying the server's ApiError.
            handleResponseExceptionWithRequest { cause, _ ->
                val response = (cause as? ResponseException)?.response ?: return@handleResponseExceptionWithRequest
                val error =
                    runCatching { response.body<ApiError>() }
                        .getOrElse {
                            ApiError(
                                "http_${response.status.value}",
                                "Server error ${response.status.value}",
                            )
                        }
                throw ApiException(response.status.value, error)
            }
        }
    }
