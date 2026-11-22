package dev.luizhenriquevicari.remir.app.navigation

import kotlinx.serialization.Serializable

/** NavHost destinations (type-safe routes). Named `Destination` to avoid clashing with the `XRoute` composables. */
sealed interface Destination {
    @Serializable
    data object Sleep : Destination

    @Serializable
    data object Faith : Destination

    @Serializable
    data object Finance : Destination

    @Serializable
    data object Habits : Destination

    @Serializable
    data object LogSleep : Destination

    @Serializable
    data object EditSleepGoal : Destination

    @Serializable
    data object Profile : Destination

    @Serializable
    data object LegalAdmin : Destination
}
