package dev.luizhenriquevicari.remir.core.sleep

import io.ktor.resources.Resource
import kotlin.time.Instant

/**
 * `GET /sleep`: the user's sleep records, newest first. Without [since] it returns the latest 100; with it, every
 * night that started at or after that moment (up to a generous cap), which is what the summary needs.
 * `POST /sleep`: log a new one.
 */
@Resource("/sleep")
class SleepResource(
    val since: Instant? = null,
)
