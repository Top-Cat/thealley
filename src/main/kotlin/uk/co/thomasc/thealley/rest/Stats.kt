package uk.co.thomasc.thealley.rest

import kotlinx.coroutines.experimental.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.LocalClient
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.Plug

var bulbs = listOf("pcroom", "landing", "bedroom", "kitchen", "frontroom", "hall").map { "lb130-$it.kirkstall.top-cat.me" }
var plugs = listOf("pcroom", "tv").map { "hs110-$it.kirkstall.top-cat.me" }

data class BulbResponse(
    val host: String,
    val name: String,
    val state: Boolean,
    val power: Int,
    val rssi: Int
)

data class PlugResponse(
    val host: String,
    val name: String,
    val state: Boolean,
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
            kasa.getDevice(host).then {
                when (it) {
                    is Plug -> PlugResponse(
                        host,
                        it.getName(),
                        it.getPowerState(),
                        it.getPowerUsage(),
                        it.getVoltage(),
                        it.getCurrent(),
                        it.getUptime(),
                        it.getSignalStrength()
                    )
                    else -> null
                }
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
            kasa.getDevice(host).then {
                when (it) {
                    is Bulb -> BulbResponse(
                        host,
                        it.getName(),
                        it.getPowerState(),
                        it.getPowerUsage(),
                        it.getSignalStrength()
                    )
                    else -> null
                }
            }
        }

        return runBlocking {
            res.mapNotNull {
                it.await()
            }
        }
    }
}
