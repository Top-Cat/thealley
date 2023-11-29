package uk.co.thomasc.thealley.rest

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.put
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IAlleyLight

@Serializable
data class ControlResult(val success: Boolean)

@Serializable
data class BulbState(val state: Int, val dimmable: Boolean, val hue: Int?, val temp: Int?, val color: Boolean) {
    constructor(on: Boolean) : this(if (on) 100 else 0, false, 0, 0, false)
    constructor(state: Int, hue: Int?, temp: Int?) : this(state, true, hue, temp, true)
}
data class SwitchData(val buttons: Map<Int, Int>)

@Location("/control")
class ControlRoute {
    @Location("/{id}")
    data class Device(val id: Int, val api: ControlRoute)

    @Location("/{id}/on")
    data class TurnOn(val id: Int, val api: ControlRoute)

    @Location("/{id}/off")
    data class TurnOff(val id: Int, val api: ControlRoute)

    @Location("/switch/{switchId}")
    data class Switch(val switchId: Int, val api: ControlRoute)
}

fun Route.controlRoute(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper) {
    suspend fun setState(id: Int, state: Boolean) =
        when (val device = deviceMapper.getDevice(id)) {
            is IAlleyLight -> {
                device.setPowerState(bus, state)
                ControlResult(true)
            }
            else -> ControlResult(false)
        }

    get<ControlRoute.TurnOn> {
        call.respond(setState(it.id, true))
    }

    get<ControlRoute.TurnOff> {
        call.respond(setState(it.id, true))
    }

    put<ControlRoute.Switch> {
        val switchData = call.receive<SwitchData>()

        // TODO: Update switch object
        /*switchData.buttons.forEach { (buttonId, sceneId) ->
            switchRepository.updateSwitch(it.switchId, buttonId, sceneId)
        }
        sceneController.resetSwitchDelegate()*/
    }

    put<ControlRoute.Device> {
        val stateRequest = call.receive<BulbState>()
        val device = deviceMapper.getDevice(it.id)

        // TODO: Support blinds
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
            else -> BulbState(false)
        }.let { bs ->
            call.respond(bs)
        }
    }

    get<ControlRoute.Device> {
        val device = deviceMapper.getDevice(it.id)

        when (device) {
            is IAlleyLight -> {
                val state = device.getLightState()
                BulbState(state.brightness ?: 0, state.hue, state.temperature)
            }
            else -> BulbState(false)
        }.let { bs ->
            call.respond(bs)
        }

        // TODO: Support blinds
        /*when (res.type) {
            SwitchRepository.DeviceType.BULB, SwitchRepository.DeviceType.RELAY, SwitchRepository.DeviceType.BLIND ->
                deviceMapper.toLight(res)?.let { l ->
                    when (l) {
                        is Bulb -> l.getLightState().let { state ->
                            BulbState(state?.brightness ?: 0, state?.hue, state?.color_temp)
                        }
                        is Blind -> BulbState(l.getState() ?: 0, null, null)
                        else -> BulbState(l.getPowerState())
                    }
                } ?: BulbState(false)
            SwitchRepository.DeviceType.PLUG, SwitchRepository.DeviceType.ZPLUG -> BulbState(false)
        }*/
    }
}
