package dev.luizhenriquevicari.remir.app.di

import dev.luizhenriquevicari.remir.app.BuildConfig
import dev.luizhenriquevicari.remir.app.auth.ClerkSession
import dev.luizhenriquevicari.remir.app.features.legal.AdminLegalViewModel
import dev.luizhenriquevicari.remir.app.features.legal.LegalApi
import dev.luizhenriquevicari.remir.app.features.legal.LegalGateViewModel
import dev.luizhenriquevicari.remir.app.features.profile.ProfileApi
import dev.luizhenriquevicari.remir.app.features.profile.ProfileViewModel
import dev.luizhenriquevicari.remir.app.features.sleep.EditSleepGoalViewModel
import dev.luizhenriquevicari.remir.app.features.sleep.LogSleepViewModel
import dev.luizhenriquevicari.remir.app.features.sleep.SleepApi
import dev.luizhenriquevicari.remir.app.features.sleep.SleepHistoryViewModel
import dev.luizhenriquevicari.remir.app.features.sleep.SleepViewModel
import dev.luizhenriquevicari.remir.app.network.createHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule =
    module {
        singleOf(::ClerkSession)
        single { createHttpClient(BuildConfig.API_URL, get<ClerkSession>()::token) }

        // profile
        singleOf(::ProfileApi)
        viewModelOf(::ProfileViewModel)

        // sleep
        singleOf(::SleepApi)
        viewModelOf(::SleepViewModel)
        viewModelOf(::SleepHistoryViewModel)
        // Logs a new night, or edits the one whose id is passed as the parameter.
        viewModel { params -> LogSleepViewModel(get(), params.getOrNull()) }
        viewModelOf(::EditSleepGoalViewModel)

        // legal
        singleOf(::LegalApi)
        viewModelOf(::LegalGateViewModel)
        viewModelOf(::AdminLegalViewModel)
    }
