package dev.luizhenriquevicari.remir.core.api

import kotlinx.serialization.Serializable

/** Generic page of results. `page` is zero-based. */
@Serializable
data class Page<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Long,
) {
    val totalPages: Int
        get() = if (size == 0) 0 else ((total + size - 1) / size).toInt()
}
