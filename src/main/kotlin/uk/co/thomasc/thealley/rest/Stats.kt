package uk.co.thomasc.thealley.rest

import kotlinx.coroutines.experimental.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.LocalClient

var bulbs = listOf("pcroom", "landing", "bedroom", "kitchen", "frontroom", "hall").map { "lb130-$it.guest.kirkstall.top-cat.me" }
var plugs = listOf("pcroom", "tv").map { "hs110-$it.guest.kirkstall.top-cat.me" }

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
class Stats(val kasa: LocalClient) {
    @GetMapping("/plug")
    fun getPowerStats(): List<PlugResponse> {
        val res = plugs.map { host ->
            kasa.getDevice(host).plug {
                PlugResponse(
                    host,
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
        val res = bulbs.map { host ->
            kasa.getDevice(host).bulb {
                BulbResponse(
                    host,
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
