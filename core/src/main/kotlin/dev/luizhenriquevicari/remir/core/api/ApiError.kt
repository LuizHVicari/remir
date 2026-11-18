package dev.luizhenriquevicari.remir.core.api

import kotlinx.serialization.Serializable

/**
 * Body of every error response from the API.
 *
 * @property code stable snake_case identifier (e.g. `validation_failed`, `not_found`).
 * @property message human-readable text.
 * @property fields per-field errors (JSON field name -> message). Empty when the error is not field-related.
 */
@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val fields: Map<String, String> = emptyMap(),
)
