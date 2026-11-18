package dev.luizhenriquevicari.remir.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.clerk.api.Clerk
import com.clerk.ui.auth.AuthView
import dev.luizhenriquevicari.remir.app.designsystem.RemirTheme
import dev.luizhenriquevicari.remir.app.designsystem.components.ErrorState
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.features.home.HomeRoute
import dev.luizhenriquevicari.remir.app.features.profile.ProfileRoute
import dev.luizhenriquevicari.remir.app.navigation.Destination

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
        else -> RemirNavHost()
    }
}

@Composable
private fun RemirNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destination.Home) {
        composable<Destination.Home> {
            HomeRoute(onOpenProfile = { navController.navigate(Destination.Profile) })
        }
        composable<Destination.Profile> {
            ProfileRoute(onBack = { navController.popBackStack() })
        }
    }
}
