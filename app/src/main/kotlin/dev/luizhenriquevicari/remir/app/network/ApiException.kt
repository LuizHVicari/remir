package dev.luizhenriquevicari.remir.app.network

import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.core.api.ApiError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Non-2xx API response, carrying the server's [ApiError]. */
class ApiException(
    val status: Int,
    val error: ApiError,
) : Exception(error.message)

/**
 * Runs an API call and returns a [Result]. Coroutine cancellation and JVM errors (`Error`, e.g. out of memory)
 * are rethrown, not wrapped: only `Exception`s become a failed [Result].
 */
suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException || it !is Exception) throw it }

/** Message ready to show on screen. The screen resolves it with `UiText.asString()`. */
val Throwable.userMessage: UiText
    get() =
        when (this) {
            is ApiException -> UiText.Dynamic(error.message)
            is IOException -> UiText.Resource(R.string.error_network)
            else -> message?.let(UiText::Dynamic) ?: UiText.Resource(R.string.error_unexpected)
        }

/** The server answered 404: the thing asked for does not exist (yet). */
val Throwable.isNotFound: Boolean
    get() = (this as? ApiException)?.status == HttpStatusCode.NotFound.value

/** `ApiError.fields` when the error came from server-side validation; empty otherwise. */
val Throwable.fieldErrors: Map<String, String>
    get() = (this as? ApiException)?.error?.fields.orEmpty()
