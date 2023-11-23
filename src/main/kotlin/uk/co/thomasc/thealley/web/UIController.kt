package uk.co.thomasc.thealley.web

import io.ktor.server.application.call
import io.ktor.server.mustache.MustacheContent
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import uk.co.thomasc.thealley.repo.SwitchRepository

fun Route.mainRoute(switchRepository: SwitchRepository) {
    get("/") {
        call.respond(
            MustacheContent(
                "home.mustache",
                mapOf(
                    "lights" to switchRepository.getDevicesForType(SwitchRepository.DeviceType.BULB) +
                        switchRepository.getDevicesForType(SwitchRepository.DeviceType.RELAY) +
                        switchRepository.getDevicesForType(SwitchRepository.DeviceType.BLIND)
                )
            )
        )
    }

    get("/external/login") {
        call.respond(MustacheContent("login.mustache", null))
    }
}
