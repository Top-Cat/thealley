package uk.co.thomasc.thealley.rest

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.repo.SwitchRepository
import uk.co.thomasc.thealley.scenes.SceneController

data class ControlResult(val success: Boolean)
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

fun Route.controlRoute(switchRepository: SwitchRepository, sceneController: SceneController, deviceMapper: DeviceMapper) {
    fun setState(id: Int, state: Boolean) =
        switchRepository.getDeviceForId(id).let { res ->

            when (res.type) {
                SwitchRepository.DeviceType.BULB, SwitchRepository.DeviceType.RELAY ->
                    deviceMapper.toLight(res)?.let {
                        it.setPowerState(state)
                        ControlResult(true)
                    } ?: ControlResult(false)
                SwitchRepository.DeviceType.PLUG -> ControlResult(false)
            }
        }

    get<ControlRoute.TurnOn> {
        call.respond(setState(it.id, true))
    }

    get<ControlRoute.TurnOff> {
        call.respond(setState(it.id, true))
    }

    put<ControlRoute.Switch> {
        val switchData = call.receive<SwitchData>()

        switchData.buttons.forEach { (buttonId, sceneId) ->
            switchRepository.updateSwitch(it.switchId, buttonId, sceneId)
        }
        sceneController.resetSwitchDelegate()
    }

    put<ControlRoute.Device> {
        val stateRequest = call.receive<BulbState>()
        val res = switchRepository.getDeviceForId(it.id)

        when (res.type) {
            SwitchRepository.DeviceType.BULB, SwitchRepository.DeviceType.RELAY ->
                deviceMapper.toLight(res)?.let {
                    it.setComplexState(
                        stateRequest.state,
                        if (stateRequest.color && stateRequest.state > 0) stateRequest.hue else null,
                        if (stateRequest.color && stateRequest.state > 0) 100 else null,
                        if (stateRequest.color && stateRequest.state > 0) stateRequest.temp else null,
                        500
                    )

                    stateRequest
                } ?: BulbState(false)
            SwitchRepository.DeviceType.PLUG -> BulbState(false)
        }.let { bs ->
            call.respond(bs)
        }
    }

    get<ControlRoute.Device> {
        val res = switchRepository.getDeviceForId(it.id)

        when (res.type) {
            SwitchRepository.DeviceType.BULB, SwitchRepository.DeviceType.RELAY ->
                deviceMapper.toLight(res)?.let { l ->
                    if (l is Bulb) {
                        val state = l.getLightState()
                        BulbState(state?.brightness ?: 0, state?.hue, state?.color_temp)
                    } else {
                        BulbState(l.getPowerState())
                    }
                } ?: BulbState(false)
            SwitchRepository.DeviceType.PLUG -> BulbState(false)
        }.let { bs ->
            call.respond(bs)
        }
    }
}
