package dev.luizhenriquevicari.remir.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import dev.luizhenriquevicari.remir.app.R

/** Destinations shown in the bottom navigation bar, in display order. */
enum class TopLevelDestination(
    val route: Destination,
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    Sleep(Destination.Sleep, R.string.tab_sleep, Icons.Filled.Bedtime),
    Faith(Destination.Faith, R.string.tab_faith, Icons.Filled.Church),
    Finance(Destination.Finance, R.string.tab_finance, Icons.Filled.AccountBalanceWallet),
    Habits(Destination.Habits, R.string.tab_habits, Icons.Filled.TaskAlt),
}
