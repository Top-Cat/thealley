package uk.co.thomasc.thealley.rest

import kotlinx.coroutines.experimental.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.repo.DeviceType
import uk.co.thomasc.thealley.repo.SwitchRepository

data class BulbResponse(
    val host: String,
    val name: String,
    val state: Int,
    val power: Int,
    val rssi: Int
)

data class PlugResponse(
    val host: String,
    val name: String,
    val state: Int,
    val power: Float,
    val voltage: Float,
    val current: Float,
    val uptime: Int,
    val rssi: Int
)

@RestController
@RequestMapping("/stats")
class Stats(val kasa: LocalClient, val switchRepository: SwitchRepository) {
    @GetMapping("/plug")
    fun getPowerStats(): List<PlugResponse> {
        val res = switchRepository.getDevicesForType(DeviceType.PLUG).map { plug ->
            kasa.getDevice(plug.hostname).plug {
                PlugResponse(
                    plug.hostname,
                    it.getName(),
                    if (it.getPowerState()) 1 else 0,
                    it.getPowerUsage(),
                    it.getVoltage(),
                    it.getCurrent(),
                    it.getUptime(),
                    it.getSignalStrength()
                )
            }
        }

        return runBlocking {
            res.mapNotNull {
                it.await()
            }
        }
    }

    @GetMapping("/bulb")
    fun getBulbStats(): List<BulbResponse> {
        val res = switchRepository.getDevicesForType(DeviceType.BULB).map { bulb ->
            kasa.getDevice(bulb.hostname).bulb {
                BulbResponse(
                    bulb.hostname,
                    it.getName(),
                    if (it.getPowerState()) 1 else 0,
                    it.getPowerUsage(),
                    it.getSignalStrength()
                )
            }
        }

        return runBlocking {
            res.mapNotNull {
                it.await()
            }
        }
    }
}
