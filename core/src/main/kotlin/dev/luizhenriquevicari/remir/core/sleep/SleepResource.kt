package dev.luizhenriquevicari.remir.core.sleep

import io.ktor.resources.Resource

/** `GET /sleep`: the user's latest sleep records, newest first. `POST /sleep`: log a new one. */
@Resource("/sleep")
class SleepResource
