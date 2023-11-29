package uk.co.thomasc.thealley.web

import io.ktor.server.application.call
import io.ktor.server.mustache.MustacheContent
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IAlleyLight

fun Route.mainRoute(deviceMapper: AlleyDeviceMapper) {
    get("/") {
        call.respond(
            MustacheContent(
                "home.mustache",
                mapOf(
                    "lights" to deviceMapper.getDevices().filterIsInstance<IAlleyLight>()
                )
            )
        )
    }

    get("/external/login") {
        call.respond(MustacheContent("login.mustache", null))
    }
}
