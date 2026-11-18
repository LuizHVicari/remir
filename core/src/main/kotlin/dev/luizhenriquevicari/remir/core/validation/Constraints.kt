package dev.luizhenriquevicari.remir.core.validation

import io.konform.validation.Constraint
import io.konform.validation.ValidationBuilder
import io.konform.validation.ValidationError

// Reusable Konform constraints. Add new shared constraints here instead of inlining them in features.

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private val URL_REGEX = Regex("^https?://[^\\s/$.?#].[^\\s]*$", RegexOption.IGNORE_CASE)

/** Not empty/whitespace-only and without leading/trailing spaces (clients trim before validating). */
fun ValidationBuilder<String>.notBlankTrimmed(): Constraint<String> =
    constrain("must not be blank or start/end with spaces") { it.isNotBlank() && it == it.trim() }

fun ValidationBuilder<String>.lengthBetween(min: Int, max: Int): Constraint<String> =
    constrain("must have between $min and $max characters") { it.length in min..max }

fun ValidationBuilder<String>.email(): Constraint<String> =
    constrain("must be a valid email address") { EMAIL_REGEX.matches(it) }

fun ValidationBuilder<String>.url(): Constraint<String> =
    constrain("must be a valid URL") { URL_REGEX.matches(it) }

/** Field name as it appears in the JSON (`.name` -> `name`, `.items[0].value` -> `items[0].value`). */
val ValidationError.field: String
    get() = dataPath.removePrefix(".")

/** Converts Konform errors into the [dev.luizhenriquevicari.remir.core.api.ApiError.fields] shape, keeping the first error per field. */
fun List<ValidationError>.byField(): Map<String, String> =
    groupBy { it.field }.mapValues { (_, errors) -> errors.first().message }
