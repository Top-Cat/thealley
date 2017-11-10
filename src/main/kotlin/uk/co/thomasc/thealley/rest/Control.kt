package uk.co.thomasc.thealley.rest

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.repo.DeviceType
import uk.co.thomasc.thealley.repo.SwitchRepository
import java.util.concurrent.TimeUnit

data class ControlResult(val success: Boolean)

@RestController
@RequestMapping("/control")
class Control(val kasa: LocalClient, val relay: RelayClient, val switchRepository: SwitchRepository) {
    @GetMapping("/on/{id}")
    fun turnOn(@PathVariable id: Int) = setState(id, true)

    @GetMapping("/off/{id}")
    fun turnOff(@PathVariable id: Int) = setState(id, false)

    fun setState(id: Int, state: Boolean): DeferredResult<ControlResult> {
        val res = switchRepository.getDeviceForId(id)
        val ret = DeferredResult<ControlResult>(TimeUnit.SECONDS.toMillis(10), ControlResult(false))

        when (res.type) {
            DeviceType.BULB -> kasa.getDevice(res.hostname).bulb {
                it?.let {
                    it.setPowerState(state)
                    ret.setResult(ControlResult(true))
                } ?: ret.setResult(ControlResult(false))
            }
            DeviceType.RELAY -> {
                relay.getRelay(res.hostname).setPowerState(state)
                ret.setResult(ControlResult(true))
            }
            DeviceType.PLUG -> ret.setResult(ControlResult(false))
        }

        return ret
    }
}
