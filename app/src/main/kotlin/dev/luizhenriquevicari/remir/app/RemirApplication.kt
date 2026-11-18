package dev.luizhenriquevicari.remir.app

import android.app.Application
import com.clerk.api.Clerk
import dev.luizhenriquevicari.remir.app.designsystem.RemirClerkTheme
import dev.luizhenriquevicari.remir.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RemirApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Without a key, App shows a configuration message instead of crashing.
        if (BuildConfig.CLERK_PUBLISHABLE_KEY.isNotBlank()) {
            Clerk.initialize(this, publishableKey = BuildConfig.CLERK_PUBLISHABLE_KEY, theme = RemirClerkTheme)
        }
        startKoin {
            androidLogger()
            androidContext(this@RemirApplication)
            modules(appModule)
        }
    }
}
