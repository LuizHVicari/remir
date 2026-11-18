package dev.luizhenriquevicari.remir.app.di

import dev.luizhenriquevicari.remir.app.BuildConfig
import dev.luizhenriquevicari.remir.app.auth.ClerkSession
import dev.luizhenriquevicari.remir.app.features.profile.ProfileApi
import dev.luizhenriquevicari.remir.app.features.profile.ProfileViewModel
import dev.luizhenriquevicari.remir.app.network.createHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::ClerkSession)
    single { createHttpClient(BuildConfig.API_URL, get<ClerkSession>()::token) }

    // profile
    singleOf(::ProfileApi)
    viewModelOf(::ProfileViewModel)
}
