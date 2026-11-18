package dev.luizhenriquevicari.remir.app.network

import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.core.api.ApiError
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Non-2xx API response, carrying the server's [ApiError]. */
class ApiException(
    val status: Int,
    val error: ApiError,
) : Exception(error.message)

/** Runs an API call and returns a [Result], without swallowing coroutine cancellation. */
suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

/** Message ready to show on screen. The screen resolves it with `UiText.asString()`. */
val Throwable.userMessage: UiText
    get() = when (this) {
        is ApiException -> UiText.Dynamic(error.message)
        is IOException -> UiText.Resource(R.string.error_network)
        else -> message?.let(UiText::Dynamic) ?: UiText.Resource(R.string.error_unexpected)
    }

/** `ApiError.fields` when the error came from server-side validation; empty otherwise. */
val Throwable.fieldErrors: Map<String, String>
    get() = (this as? ApiException)?.error?.fields.orEmpty()
