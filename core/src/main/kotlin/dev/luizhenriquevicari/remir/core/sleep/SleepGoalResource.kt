package dev.luizhenriquevicari.remir.core.sleep

import io.ktor.resources.Resource

/** `GET /sleep/goal`: the user's goal, 404 when none is set. `PUT /sleep/goal`: set or replace it. */
@Resource("/sleep/goal")
class SleepGoalResource
