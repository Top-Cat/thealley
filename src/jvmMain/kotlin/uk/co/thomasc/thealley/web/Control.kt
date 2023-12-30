package uk.co.thomasc.thealley.web

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.put
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IAlleyLight
import uk.co.thomasc.thealley.devices.IAlleyRelay

data class SwitchData(val buttons: Map<Int, Int>)

@Location("/control")
class ControlRoute : IAlleyRoute {
    @Location("/list")
    data class ListRoute(val api: ControlRoute)

    @Location("/multi/{ids}")
    data class Multi(val ids: String, val api: ControlRoute)

    @Location("/{id}")
    data class Device(val id: Int, val api: ControlRoute)

    @Location("/{id}/on")
    data class TurnOn(val id: Int, val api: ControlRoute)

    @Location("/{id}/off")
    data class TurnOff(val id: Int, val api: ControlRoute)

    @Location("/switch/{switchId}")
    data class Switch(val switchId: Int, val api: ControlRoute)

    suspend fun setState(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper, id: Int, state: Boolean) =
        when (val device = deviceMapper.getDevice(id)) {
            is IAlleyLight -> {
                device.setPowerState(bus, state)
                ControlResult(true)
            }
            else -> ControlResult(false)
        }

    override fun Route.setup(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper) {
        get<ListRoute> {
            val devices = deviceMapper.getDevices()
                .mapNotNull { if (it is IAlleyRelay) DeviceInfo(it.id, it.config) else null }
            call.respond(devices)
        }

        get<TurnOn> {
            call.respond(setState(bus, deviceMapper, it.id, true))
        }

        get<TurnOff> {
            call.respond(setState(bus, deviceMapper, it.id, true))
        }

        put<Switch> {
            val switchData = call.receive<SwitchData>()

            // TODO: Update switch object
            /*switchData.buttons.forEach { (buttonId, sceneId) ->
                switchRepository.updateSwitch(it.switchId, buttonId, sceneId)
            }
            sceneController.resetSwitchDelegate()*/
        }

        put<Device> {
            val stateRequest = call.receive<BulbState>()
            val device = deviceMapper.getDevice(it.id)

            when (device) {
                is IAlleyLight -> {
                    device.setComplexState(
                        bus,
                        IAlleyLight.LightState(
                            stateRequest.state,
                            if (stateRequest.color && stateRequest.state > 0) stateRequest.hue else null,
                            if (stateRequest.color && stateRequest.state > 0) 100 else null,
                            if (stateRequest.color && stateRequest.state > 0) stateRequest.temp else null
                        ),
                        500
                    )

                    stateRequest
                }
                is IAlleyRelay -> {
                    device.setPowerState(bus, stateRequest.state > 50)
                    BulbState(stateRequest.state > 50)
                }
                else -> BulbState(false)
            }.let { bs ->
                call.respond(bs)
            }
        }

        suspend fun getStateForId(id: Int) =
            when (val device = deviceMapper.getDevice(id)) {
                is IAlleyLight -> {
                    val state = device.getLightState()
                    BulbState(state.brightness ?: 0, state.hue, state.temperature)
                }
                is IAlleyRelay -> BulbState(device.getPowerState())
                else -> BulbState(false)
            }

        get<Multi> { req ->
            val ids = req.ids.split(",").mapNotNull { n -> n.toIntOrNull() }

            val response = ids.asFlow().flatMapMerge(30) {
                flow {
                    emit(it to getStateForId(it))
                }
            }.toList().toMap()

            call.respond(response)
        }

        get<Device> {
            call.respond(getStateForId(it.id))
        }
    }
}
