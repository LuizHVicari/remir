package dev.luizhenriquevicari.remir.app.common

import java.time.LocalDate
import java.time.LocalTime
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
): String = toJavaInstant().atZone(zone).toLocalDate().toLocalizedDate(locale)

/** The time of day of this instant in the device's time zone, in the device's 12/24h style. */
fun Instant.toLocalizedTime(
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
): String = toJavaInstant().atZone(zone).toLocalTime().toLocalizedTime(locale)

fun Instant.toLocalizedMonth(
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
): String =
    DateTimeFormatter
        .ofPattern("LLLL yyyy", locale)
        .format(toJavaInstant().atZone(zone))
        .replaceFirstChar { it.titlecase(locale) }

fun Instant.toLocalizedWeekday(
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
): String =
    DateTimeFormatter
        .ofPattern("EEE", locale)
        .format(toJavaInstant().atZone(zone))
        .replaceFirstChar { it.titlecase(locale) }

fun Instant.toDayOfMonth(zone: ZoneId = ZoneId.systemDefault()): Int = toJavaInstant().atZone(zone).dayOfMonth

fun LocalDate.toLocalizedDate(locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(this)

fun LocalTime.toLocalizedTime(locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).format(this)
