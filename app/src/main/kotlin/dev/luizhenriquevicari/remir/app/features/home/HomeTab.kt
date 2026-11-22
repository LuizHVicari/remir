package dev.luizhenriquevicari.remir.app.features.home

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import dev.luizhenriquevicari.remir.app.R

/** Sections reachable from the home bottom navigation bar. */
enum class HomeTab(
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    Sleep(R.string.home_tab_sleep, Icons.Filled.Bedtime),
    Faith(R.string.home_tab_faith, Icons.Filled.Church),
    Finance(R.string.home_tab_finance, Icons.Filled.AccountBalanceWallet),
    Habits(R.string.home_tab_habits, Icons.Filled.TaskAlt),
}
