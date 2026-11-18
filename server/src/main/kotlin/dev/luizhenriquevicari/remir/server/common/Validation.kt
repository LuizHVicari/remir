package dev.luizhenriquevicari.remir.server.common

import dev.luizhenriquevicari.remir.core.validation.byField
import io.konform.validation.Invalid
import io.konform.validation.Validation

/** Validates with the `core` validator; throws [ValidationFailed] with `fields` filled when invalid. */
fun <T> Validation<T>.orThrow(value: T): T {
    val result = this(value)
    if (result is Invalid) throw ValidationFailed(result.errors.byField())
    return value
}
