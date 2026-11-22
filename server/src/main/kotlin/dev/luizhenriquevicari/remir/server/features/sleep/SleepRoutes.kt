package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SaveSleepGoalRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalResource
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalValidation
import dev.luizhenriquevicari.remir.core.sleep.SleepResource
import dev.luizhenriquevicari.remir.core.sleep.SleepValidation
import dev.luizhenriquevicari.remir.server.common.orThrow
import dev.luizhenriquevicari.remir.server.common.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

/** Plain HTTP: receive, validate (with the `core` validator), call the service, respond. */
fun Route.sleepRoutes(service: SleepService) {
    get<SleepResource> {
        call.respond(service.list(call.userId()))
    }

    post<SleepResource> {
        val request = SleepValidation.create.orThrow(call.receive<CreateSleepRecordRequest>())
        call.respond(HttpStatusCode.Created, service.create(call.userId(), request))
    }

    get<SleepGoalResource> {
        call.respond(service.goal(call.userId()))
    }

    put<SleepGoalResource> {
        val request = SleepGoalValidation.save.orThrow(call.receive<SaveSleepGoalRequest>())
        call.respond(service.saveGoal(call.userId(), request))
    }
}
