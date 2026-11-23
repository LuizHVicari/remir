package dev.luizhenriquevicari.remir.core.sleep

import dev.luizhenriquevicari.remir.core.validation.byField
import io.konform.validation.Invalid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class SleepValidationTest {
    private val bedtime = Instant.parse("2026-03-10T23:00:00Z")
    private val fellAsleep = Instant.parse("2026-03-10T23:30:00Z")
    private val wokeUp = Instant.parse("2026-03-11T07:00:00Z")
    private val gotUp = Instant.parse("2026-03-11T07:15:00Z")

    private fun request(
        fellAsleepAt: Instant? = fellAsleep,
        wokeUpAt: Instant = wokeUp,
        gotUpAt: Instant = gotUp,
    ) = CreateSleepRecordRequest(bedtime, fellAsleepAt, wokeUpAt, gotUpAt)

    private fun invalidFields(request: CreateSleepRecordRequest) =
        assertIs<Invalid>(SleepValidation.create(request)).errors.byField().keys

    @Test
    fun `a complete night passes`() {
        assertTrue(SleepValidation.create(request()).isValid)
    }

    @Test
    fun `fell asleep time is optional`() {
        assertTrue(SleepValidation.create(request(fellAsleepAt = null)).isValid)
    }

    @Test
    fun `waking up at bedtime fails on wokeUpAt`() {
        val fields = invalidFields(request(fellAsleepAt = null, wokeUpAt = bedtime, gotUpAt = bedtime))
        assertEquals(setOf("wokeUpAt"), fields)
    }

    @Test
    fun `fell asleep before bedtime fails on fellAsleepAt`() {
        assertEquals(setOf("fellAsleepAt"), invalidFields(request(fellAsleepAt = bedtime - 5.minutes)))
    }

    @Test
    fun `fell asleep after waking up fails on fellAsleepAt`() {
        assertEquals(setOf("fellAsleepAt"), invalidFields(request(fellAsleepAt = wokeUp + 1.minutes)))
    }

    @Test
    fun `getting up before waking up fails on gotUpAt`() {
        assertEquals(setOf("gotUpAt"), invalidFields(request(gotUpAt = wokeUp - 1.minutes)))
    }

    @Test
    fun `more than 24 hours between bedtime and getting up fails on gotUpAt`() {
        val late = bedtime + 25.hours
        assertEquals(setOf("gotUpAt"), invalidFields(request(fellAsleepAt = null, wokeUpAt = late, gotUpAt = late)))
    }
}
