package dev.luizhenriquevicari.remir.app.common

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * The calendar date of this instant in the device's time zone, formatted for the device's language
 * (e.g. "Jan 10, 2026" or "10 de jan. de 2026"). An `Instant` is UTC, so the zone matters near midnight.
 */
fun Instant.toLocalizedDate(
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
): String =
    DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
        .format(toJavaInstant().atZone(zone))
