package dev.luizhenriquevicari.remir.core.api

import kotlinx.serialization.json.Json

/** Single JSON configuration, shared by the server (ContentNegotiation) and the app (Ktor Client). */
val RemirJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
