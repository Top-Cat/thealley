package uk.co.thomasc.thealley.web

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import uk.co.thomasc.thealley.checkOauth
import uk.co.thomasc.thealley.oauth.AlleyTokenStore
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.web.google.GoogleHomeReq

@Location("/external")
class ExternalRoute(private val alleyTokenStore: AlleyTokenStore) : IAlleyRoute {
    @Location("/googlehome")
    data class GoogleHome(val api: ExternalRoute)

    @Location("/test")
    data class Test(val api: ExternalRoute)

    override fun Route.setup(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper) {
        get<Test> {
            checkOauth(alleyTokenStore) {
                call.respond("Hi")
            }
        }

        val externalHandler = ExternalHandler(deviceMapper)
        post<GoogleHome> {
            checkOauth(alleyTokenStore) { userId ->
                val obj = call.receive<GoogleHomeReq>()
                call.respond(externalHandler.handleRequest(userId, obj))
            }
        }
    }
}
