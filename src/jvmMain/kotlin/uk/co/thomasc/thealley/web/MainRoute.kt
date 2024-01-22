package uk.co.thomasc.thealley.web

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.mustache.MustacheContent
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay

@Location("/")
class MainRoute : IAlleyRoute {
    @Location("/")
    data class Home(val api: MainRoute)

    override fun Route.setup(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper) {
        get<Home> {
            call.respond(
                MustacheContent(
                    "home.mustache",
                    mapOf(
                        "lights" to deviceMapper.getDevices().filterIsInstance<IAlleyRelay>()
                    )
                )
            )
        }

        get("/external/login") {
            call.respond(MustacheContent("login.mustache", null))
        }
    }
}
