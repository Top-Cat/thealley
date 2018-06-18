package uk.co.thomasc.thealley.rest

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.client.TadoClient
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.devices.Plug
import uk.co.thomasc.thealley.devices.Relay
import uk.co.thomasc.thealley.repo.SwitchRepository

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BulbResponse(
    val host: String,
    val name: String?,
    val state: Int,
    val power: Int,
    val rssi: Int?
)

data class RelayResponse(
    val host: String,
    val state: Int,
    val extra: Map<String, Any>
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
class Stats(val switchRepository: SwitchRepository, val tadoClient: TadoClient, val deviceMapper: DeviceMapper) {
    @GetMapping("/plug")
    fun getPowerStats() =
        switchRepository.getDevicesForType(SwitchRepository.DeviceType.PLUG).mapNotNull {
            plug ->

            try {
                Plug(plug.hostname).let {
                    it.updateData()
                    val power = it.getPower()

                     PlugResponse(
                        plug.hostname,
                        it.getName(),
                        if (it.getPowerState()) 1 else 0,
                        power.power,
                        power.voltage,
                        power.current,
                        it.getUptime(),
                        it.getSignalStrength()
                    )
                }
            } catch (e: KotlinNullPointerException) {
                null
            }
        }

    @GetMapping("/bulb")
    fun getBulbStats() =
        deviceMapper.each(switchRepository.getDevicesForType(SwitchRepository.DeviceType.BULB)) {
            bulb, dev ->

            (bulb as? Bulb)?.let {
                // Power update will cause sysinfo update
                val power = bulb.getPowerUsage()

                BulbResponse(
                    dev.hostname,
                    bulb.getName(),
                    if (bulb.getPowerState()) 1 else 0,
                    power,
                    bulb.getSignalStrength()
                )
            }
        }

    @GetMapping("/relay")
    fun getRelayStats() =
        deviceMapper.each(switchRepository.getDevicesForType(SwitchRepository.DeviceType.RELAY)) {
            relay, dev ->

            (relay as? Relay)?.let {
                RelayResponse(
                    dev.hostname,
                    if (relay.getPowerState()) 1 else 0,
                    relay.props
                )
            }
        }

    @GetMapping("/tado")
    fun getTadoStats() = tadoClient.getState()
}
