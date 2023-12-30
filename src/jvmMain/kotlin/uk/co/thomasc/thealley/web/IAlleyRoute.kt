package uk.co.thomasc.thealley.web

import io.ktor.server.routing.Route
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus

interface IAlleyRoute {
    fun Route.setup(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper)
}
