package uk.co.thomasc.thealley.rest

import com.fasterxml.jackson.annotation.JsonInclude
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.client.TadoClient
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.devices.Plug
import uk.co.thomasc.thealley.devices.Relay
import uk.co.thomasc.thealley.devices.ZPlugState
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

@Location("/stats")
class StatsRoute {
    @Location("/plug")
    data class Plug(val api: StatsRoute)
    @Location("/bulb")
    data class Bulb(val api: StatsRoute)
    @Location("/relay")
    data class Relay(val api: StatsRoute)
    @Location("/tado")
    data class Tado(val api: StatsRoute)
}

fun Route.statsRoute(switchRepository: SwitchRepository, tadoClient: TadoClient, deviceMapper: DeviceMapper, mqtt: RelayClient) {
    suspend fun getPlugs() = switchRepository.getDevicesForType(SwitchRepository.DeviceType.PLUG).asFlow().flatMapMerge(10) { plug ->
        flow {
            try {
                Plug(plug.hostname).let {
                    it.updateData()
                    val power = it.getPower()

                    PlugResponse(
                        plug.hostname,
                        it.getName() ?: "",
                        if (it.getPowerState() == true) 1 else 0,
                        power.power,
                        power.voltage,
                        power.current,
                        it.getUptime() ?: -1,
                        it.getSignalStrength() ?: -1
                    )
                }.also {
                    emit(it)
                }
            } catch (e: KotlinNullPointerException) {
                // Ignore
            }
        }
    }

    suspend fun getZPlugs() = switchRepository.getDevicesForType(SwitchRepository.DeviceType.ZPLUG).asFlow().flatMapMerge(10) { plug ->
        flow {
            try {
                mqtt.getZPlug(plug.hostname).let {
                    it.getState()?.let { state ->
                        PlugResponse(
                            plug.hostname,
                            plug.name,
                            if (state.state == ZPlugState.ON) 1 else 0,
                            state.power,
                            state.voltage,
                            state.current,
                            -1,
                            state.linkquality
                        )
                    }
                }.also {
                    emit(it)
                }
            } catch (e: KotlinNullPointerException) {
                // Ignore
            }
        }
    }

    get<StatsRoute.Plug> {
        call.respond(kotlinx.coroutines.flow.merge(getPlugs(), getZPlugs()).toList())
    }

    get<StatsRoute.Bulb> {
        deviceMapper.each(switchRepository.getDevicesForType(SwitchRepository.DeviceType.BULB)) { bulb, dev ->
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
        }.let {
            call.respond(it)
        }
    }

    get<StatsRoute.Relay> {
        deviceMapper.each(switchRepository.getDevicesForType(SwitchRepository.DeviceType.RELAY)) { relay, dev ->
            (relay as? Relay)?.let {
                RelayResponse(
                    dev.hostname,
                    if (relay.getPowerState()) 1 else 0,
                    relay.props
                )
            }
        }.let {
            call.respond(it)
        }
    }

    get<StatsRoute.Tado> {
        call.respond(tadoClient.getState())
    }
}
