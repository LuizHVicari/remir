package dev.luizhenriquevicari.remir.server.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

/**
 * A `TIMESTAMPTZ` column exposed as a `kotlin.time.Instant`, the type the `core` DTOs use.
 *
 * Always use this instead of Exposed's `timestamp()`: that one creates a `TIMESTAMP` column without a time zone and
 * writes the value in the JVM's default zone, so the stored data would depend on where the server runs.
 */
fun Table.timestamptz(name: String): Column<Instant> =
    timestampWithTimeZone(name).transform(
        wrap = { dateTime: OffsetDateTime -> dateTime.toInstant().toKotlinInstant() },
        unwrap = { instant: Instant -> OffsetDateTime.ofInstant(instant.toJavaInstant(), ZoneOffset.UTC) },
    )
