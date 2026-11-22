package dev.luizhenriquevicari.remir.core.sleep

import io.ktor.resources.Resource
import kotlin.time.Instant

/**
 * `GET /sleep`: the user's sleep records, newest first.
 *
 * - [since]: only nights that started at or after that moment (what the summary needs).
 * - [limit] and [offset]: one page of the list, so a client can scroll through everything. Without [limit] the
 *   server picks one: 100 nights, or up to 400 when [since] is given. It is capped at 400 either way.
 *
 * `POST /sleep`: log a new one.
 */
@Resource("/sleep")
class SleepResource(
    val since: Instant? = null,
    val limit: Int? = null,
    val offset: Int = 0,
)
