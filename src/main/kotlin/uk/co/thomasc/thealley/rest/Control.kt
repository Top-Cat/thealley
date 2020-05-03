package uk.co.thomasc.thealley.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.repo.SwitchRepository
import java.util.concurrent.TimeUnit

data class ControlResult(val success: Boolean)
data class BulbState(val state: Int, val dimmable: Boolean, val hue: Int?, val temp: Int?, val color: Boolean) {
    constructor(on: Boolean): this(if (on) 100 else 0, false, 0, 0, false)
    constructor(state: Int, hue: Int?, temp: Int?): this(state, true, hue, temp, true)
}

@RestController
@RequestMapping("/control")
class Control(val relay: RelayClient, val switchRepository: SwitchRepository, val deviceMapper: DeviceMapper) {
    @GetMapping("/{id}/on")
    fun turnOn(@PathVariable id: Int) = setState(id, true)

    @GetMapping("/{id}/off")
    fun turnOff(@PathVariable id: Int) = setState(id, false)

    fun setState(id: Int, state: Boolean): DeferredResult<ControlResult> {
        val res = switchRepository.getDeviceForId(id)
        val ret = DeferredResult<ControlResult>(TimeUnit.SECONDS.toMillis(10), ControlResult(false))

        when (res.type) {
            SwitchRepository.DeviceType.BULB, SwitchRepository.DeviceType.RELAY ->
                deviceMapper.toLight(res)?.let {
                        it.setPowerState(state)
                        ret.setResult(ControlResult(true))
                } ?: ret.setResult(ControlResult(false))
            SwitchRepository.DeviceType.PLUG -> ret.setResult(ControlResult(false))
        }

        return ret
    }

    @PutMapping("/{id}")
    fun setStatePut(@PathVariable id: Int, @RequestBody stateRequest: BulbState): DeferredResult<BulbState> {
        val res = switchRepository.getDeviceForId(id)
        val ret = DeferredResult<BulbState>(TimeUnit.SECONDS.toMillis(10), ControlResult(false))

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

                    if (it is Bulb) {
                        val state = it.getLightState()
                        ret.setResult(BulbState(state.brightness ?: 0, state.hue ?: 0, state.color_temp ?: 0))
                    } else {
                        ret.setResult(BulbState(it.getPowerState()))
                    }
                } ?: ret.setResult(BulbState(false))
            SwitchRepository.DeviceType.PLUG -> ret.setResult(BulbState(false))
        }

        return ret
    }

    @GetMapping("/{id}")
    fun getState(@PathVariable id: Int): DeferredResult<BulbState> {
        val res = switchRepository.getDeviceForId(id)
        val ret = DeferredResult<BulbState>(TimeUnit.SECONDS.toMillis(10), ControlResult(false))

        when (res.type) {
            SwitchRepository.DeviceType.BULB, SwitchRepository.DeviceType.RELAY ->
                deviceMapper.toLight(res)?.let {
                    if (it is Bulb) {
                        val state = it.getLightState()
                        ret.setResult(BulbState(state.brightness ?: 0, state.hue, state.color_temp))
                    } else {
                        ret.setResult(BulbState(it.getPowerState()))
                    }
                } ?: ret.setResult(BulbState(false))
            SwitchRepository.DeviceType.PLUG -> ret.setResult(BulbState(false))
        }

        return ret
    }
}
