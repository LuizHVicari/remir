package dev.luizhenriquevicari.remir.core.sleep

import io.konform.validation.Validation

object SleepGoalValidation {
    const val MIN_SLEEP_MINUTES = 60
    const val MAX_SLEEP_MINUTES = 960

    /** Key of the error that belongs to the whole goal and not to one field (`ValidationError.field` of the root). */
    const val WHOLE_GOAL = ""

    val save =
        Validation<SaveSleepGoalRequest> {
            constrain("set at least one of bedtime, wake up time or sleep duration") {
                it.bedtime != null || it.wakeUpTime != null || it.sleepMinutes != null
            }
            SaveSleepGoalRequest::sleepMinutes ifPresent {
                constrain("must be between 1 and 16 hours") { it in MIN_SLEEP_MINUTES..MAX_SLEEP_MINUTES }
            }
        }
}
