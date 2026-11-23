package dev.luizhenriquevicari.remir.core.sleep

import io.ktor.resources.Resource
import kotlin.uuid.Uuid

/** `GET`, `PUT` (same body as logging a night) and `DELETE /sleep/{id}`. 404 when missing or not the caller's. */
@Resource("/sleep/{id}")
class SleepRecordResource(
    val id: Uuid,
)
