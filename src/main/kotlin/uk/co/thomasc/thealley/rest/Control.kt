package uk.co.thomasc.thealley.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.repo.SwitchRepository
import java.util.concurrent.TimeUnit

data class ControlResult(val success: Boolean)
data class BulbState(val state: Boolean)

@RestController
@RequestMapping("/control")
class Control(val kasa: LocalClient, val relay: RelayClient, val switchRepository: SwitchRepository) {
    @GetMapping("/{id}/on")
    fun turnOn(@PathVariable id: Int) = setState(id, true)

    @GetMapping("/{id}/off")
    fun turnOff(@PathVariable id: Int) = setState(id, false)

    fun setState(id: Int, state: Boolean): DeferredResult<ControlResult> {
        val res = switchRepository.getDeviceForId(id)
        val ret = DeferredResult<ControlResult>(TimeUnit.SECONDS.toMillis(10), ControlResult(false))

        when (res.type) {
            SwitchRepository.DeviceType.BULB -> kasa.getDevice(res.hostname).bulb {
                it?.let {
                    it.setPowerState(state)
                    ret.setResult(ControlResult(true))
                } ?: ret.setResult(ControlResult(false))
            }
            SwitchRepository.DeviceType.RELAY -> {
                relay.getRelay(res.hostname).setPowerState(state)
                ret.setResult(ControlResult(true))
            }
            SwitchRepository.DeviceType.PLUG -> ret.setResult(ControlResult(false))
        }

        return ret
    }

    @GetMapping("/{id}")
    fun getState(@PathVariable id: Int): DeferredResult<BulbState> {
        val res = switchRepository.getDeviceForId(id)
        val ret = DeferredResult<BulbState>(TimeUnit.SECONDS.toMillis(10), ControlResult(false))

        when (res.type) {
            SwitchRepository.DeviceType.BULB -> kasa.getDevice(res.hostname).bulb {
                it?.let {
                    ret.setResult(BulbState(it.getPowerState()))
                } ?: ret.setResult(BulbState(false))
            }
            SwitchRepository.DeviceType.RELAY -> {
                ret.setResult(BulbState(relay.getRelay(res.hostname).getState()))
            }
            SwitchRepository.DeviceType.PLUG -> ret.setResult(BulbState(false))
        }

        return ret
    }
}
