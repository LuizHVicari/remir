package dev.luizhenriquevicari.remir.app.navigation

import kotlinx.serialization.Serializable

/** NavHost destinations (type-safe routes). Named `Destination` to avoid clashing with the `XRoute` composables. */
sealed interface Destination {
    @Serializable
    data object Home : Destination

    @Serializable
    data object Profile : Destination
}
