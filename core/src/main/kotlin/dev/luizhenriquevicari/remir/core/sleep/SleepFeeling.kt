package dev.luizhenriquevicari.remir.core.sleep

import kotlinx.serialization.Serializable

/** How the user felt on waking up, from worst to best. The order of the entries is the order of the scale. */
@Serializable
enum class SleepFeeling {
    EXHAUSTED,
    TIRED,
    OK,
    RESTED,
    ENERGIZED,
}
