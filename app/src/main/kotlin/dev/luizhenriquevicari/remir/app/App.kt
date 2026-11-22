package dev.luizhenriquevicari.remir.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.clerk.api.Clerk
import com.clerk.ui.auth.AuthView
import dev.luizhenriquevicari.remir.app.designsystem.RemirTheme
import dev.luizhenriquevicari.remir.app.designsystem.components.ErrorState
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.features.faith.FaithScreen
import dev.luizhenriquevicari.remir.app.features.finance.FinanceScreen
import dev.luizhenriquevicari.remir.app.features.habits.HabitsScreen
import dev.luizhenriquevicari.remir.app.features.legal.AdminLegalRoute
import dev.luizhenriquevicari.remir.app.features.legal.LegalGate
import dev.luizhenriquevicari.remir.app.features.profile.ProfileRoute
import dev.luizhenriquevicari.remir.app.features.sleep.EditSleepGoalRoute
import dev.luizhenriquevicari.remir.app.features.sleep.LogSleepRoute
import dev.luizhenriquevicari.remir.app.features.sleep.SleepRoute
import dev.luizhenriquevicari.remir.app.navigation.Destination
import dev.luizhenriquevicari.remir.app.navigation.RemirNavigationBar
import dev.luizhenriquevicari.remir.app.navigation.TopLevelDestination

@Composable
fun App() {
    RemirTheme {
        Surface(Modifier.fillMaxSize()) {
            if (BuildConfig.CLERK_PUBLISHABLE_KEY.isBlank()) {
                ErrorState(message = stringResource(R.string.missing_clerk_key))
                return@Surface
            }
            SessionGate()
        }
    }
}

/** Signed out: Clerk's prebuilt sign-in/sign-up UI. Signed in: the main navigation. */
@Composable
private fun SessionGate() {
    val initialized by Clerk.isInitialized.collectAsStateWithLifecycle()
    val initializationError by Clerk.initializationError.collectAsStateWithLifecycle()
    val signedIn by Clerk.isAuthFlowCompleteFlow.collectAsStateWithLifecycle()

    when {
        initializationError != null ->
            ErrorState(message = stringResource(R.string.clerk_init_failed, initializationError?.message.orEmpty()))
        !initialized -> Loading()
        // TODO(verify): Clerk's KDoc warns that AuthView loses typed input when the Activity is its
        // ViewModelStoreOwner (e.g. on rotation). If that bites, host AuthView in a navigation destination.
        !signedIn -> AuthView(modifier = Modifier.safeDrawingPadding(), isDismissible = false)
        else -> LegalGate { RemirNavHost() }
    }
}

@Composable
private fun RemirNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Null on the full-screen destinations (profile, admin), which hides the bottom bar.
    val currentTab =
        TopLevelDestination.entries.firstOrNull { backStackEntry?.destination?.hasRoute(it.route::class) == true }
    val openProfile = { navController.navigate(Destination.Profile) }

    Scaffold(
        // Each destination's own Scaffold handles the system bar insets; this one only reserves the bar's height.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            currentTab?.let { RemirNavigationBar(selected = it, onSelect = navController::navigateToTab) }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Sleep,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable<Destination.Sleep> {
                SleepRoute(
                    onOpenProfile = openProfile,
                    onLogSleep = { navController.navigate(Destination.LogSleep) },
                    onEditGoal = { navController.navigate(Destination.EditSleepGoal) },
                )
            }
            composable<Destination.EditSleepGoal> { EditSleepGoalRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.LogSleep> { LogSleepRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.Faith> { FaithScreen(onOpenProfile = openProfile) }
            composable<Destination.Finance> { FinanceScreen(onOpenProfile = openProfile) }
            composable<Destination.Habits> { HabitsScreen(onOpenProfile = openProfile) }
            composable<Destination.Profile> {
                ProfileRoute(
                    onBack = { navController.popBackStack() },
                    onOpenLegalAdmin = { navController.navigate(Destination.LegalAdmin) },
                )
            }
            composable<Destination.LegalAdmin> {
                AdminLegalRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Switches tab keeping one back stack per tab: state is saved on leave and restored on return. */
private fun NavHostController.navigateToTab(tab: TopLevelDestination) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
