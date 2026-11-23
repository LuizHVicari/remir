package dev.luizhenriquevicari.remir.core.sleep

import dev.luizhenriquevicari.remir.core.validation.byField
import io.konform.validation.Invalid
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SleepGoalValidationTest {
    private val bedtime = LocalTime.parse("23:00")
    private val wakeUp = LocalTime.parse("07:00")

    private fun invalidFields(request: SaveSleepGoalRequest) =
        assertIs<Invalid>(SleepGoalValidation.save(request)).errors.byField().keys

    @Test
    fun `any single part is enough`() {
        assertTrue(SleepGoalValidation.save(SaveSleepGoalRequest(bedtime = bedtime)).isValid)
        assertTrue(SleepGoalValidation.save(SaveSleepGoalRequest(wakeUpTime = wakeUp)).isValid)
        assertTrue(SleepGoalValidation.save(SaveSleepGoalRequest(sleepMinutes = 480)).isValid)
    }

    @Test
    fun `all three parts together pass`() {
        assertTrue(SleepGoalValidation.save(SaveSleepGoalRequest(bedtime, wakeUp, 480)).isValid)
    }

    @Test
    fun `an empty goal fails on the whole goal, not on a field`() {
        assertEquals(setOf(SleepGoalValidation.WHOLE_GOAL), invalidFields(SaveSleepGoalRequest()))
    }

    @Test
    fun `sleep duration outside 1 to 16 hours fails on sleepMinutes`() {
        assertEquals(setOf("sleepMinutes"), invalidFields(SaveSleepGoalRequest(sleepMinutes = 30)))
        assertEquals(setOf("sleepMinutes"), invalidFields(SaveSleepGoalRequest(sleepMinutes = 17 * 60)))
    }
}
