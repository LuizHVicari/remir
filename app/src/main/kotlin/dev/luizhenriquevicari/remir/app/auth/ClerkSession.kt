package dev.luizhenriquevicari.remir.app.auth

import android.util.Log
import com.clerk.api.Clerk
import com.clerk.api.network.serialization.ClerkResult
import com.clerk.api.network.serialization.errorMessage

/** Access to the Clerk SDK outside the UI (per-request token and sign-out). */
class ClerkSession {
    /**
     * Token of the current session. Called on EVERY request: the SDK caches and refreshes it
     * (Clerk tokens are short-lived). Never store the token anywhere.
     */
    suspend fun token(): String? =
        when (val result = Clerk.auth.getToken()) {
            is ClerkResult.Success -> result.value
            is ClerkResult.Failure -> {
                Log.w(TAG, "No Clerk token: ${result.errorMessage}")
                null
            }
        }

    /** Ends the Clerk session. The gate in `App` switches back to sign-in on its own. */
    suspend fun signOut(): Result<Unit> =
        when (val result = Clerk.auth.signOut()) {
            is ClerkResult.Success -> Result.success(Unit)
            is ClerkResult.Failure -> Result.failure(IllegalStateException(result.errorMessage))
        }

    private companion object {
        const val TAG = "ClerkSession"
    }
}
