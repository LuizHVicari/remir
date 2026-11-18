package dev.luizhenriquevicari.remir.core.profile

import io.ktor.resources.Resource

/** `GET /me` (provisions the user on first access) and `PUT /me`. */
@Resource("/me")
class MeResource
