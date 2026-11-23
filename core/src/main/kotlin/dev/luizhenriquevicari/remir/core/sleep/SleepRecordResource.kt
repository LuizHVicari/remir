package dev.luizhenriquevicari.remir.core.sleep

import io.ktor.resources.Resource
import kotlin.uuid.Uuid

/**
 * One night: `GET /sleep/{id}` reads it, `PUT /sleep/{id}` replaces it as a whole (same body as logging a night),
 * `DELETE /sleep/{id}` removes it. All answer 404 when the night does not exist or belongs to someone else.
 */
@Resource("/sleep/{id}")
class SleepRecordResource(
    val id: Uuid,
)
