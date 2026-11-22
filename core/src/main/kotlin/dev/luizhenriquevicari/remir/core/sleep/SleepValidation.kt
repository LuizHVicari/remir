package dev.luizhenriquevicari.remir.core.sleep

import io.konform.validation.Validation
import kotlin.time.Duration.Companion.hours

object SleepValidation {
    /** Bedtime to getting up cannot span more than this; it catches wrong dates (e.g. a missing day change). */
    val MAX_SPAN = 24.hours

    /** The four moments must be in order: bedtime <= fell asleep <= woke up <= got up. */
    val create =
        Validation<CreateSleepRecordRequest> {
            dynamic { request ->
                CreateSleepRecordRequest::fellAsleepAt ifPresent {
                    constrain("must not be before bedtime") { it >= request.bedtime }
                    constrain("must not be after waking up") { it <= request.wokeUpAt }
                }
                CreateSleepRecordRequest::wokeUpAt {
                    constrain("must be after bedtime") { it > request.bedtime }
                }
                CreateSleepRecordRequest::gotUpAt {
                    constrain("must not be before waking up") { it >= request.wokeUpAt }
                    constrain("must be within 24 hours of bedtime") { it - request.bedtime <= MAX_SPAN }
                }
            }
        }
}
