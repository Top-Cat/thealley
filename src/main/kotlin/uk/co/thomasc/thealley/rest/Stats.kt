package uk.co.thomasc.thealley.rest

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.energy.tado.TadoDevice
import uk.co.thomasc.thealley.devicev2.kasa.bulb.BulbDevice
import uk.co.thomasc.thealley.devicev2.kasa.plug.PlugDevice
import uk.co.thomasc.thealley.devicev2.relay.RelayDevice

@Serializable
data class BulbResponse(
    val host: String,
    val name: String? = null,
    val state: Int,
    val power: Int,
    val rssi: Int? = null
)

@Serializable
data class RelayResponse(
    val host: String,
    val state: Int,
    val extra: Map<String, JsonElement>
)

@Serializable
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

@Serializable
data class TadoResponse(
    val homeId: Int,
    val zoneStates: List<TransformedZoneState>
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

    @Location("/tado-multi")
    data class TadoMulti(val api: StatsRoute)
}

suspend inline fun <reified T : AlleyDevice<*, *, *>, U> getStats(devices: AlleyDeviceMapper, crossinline block: suspend (T) -> U, concurrency: Int = 10) =
    devices.getDevices<T>().asFlow().flatMapMerge(concurrency) { device ->
        flow {
            emit(
                block(device)
            )
        }
    }.toList()

fun Route.statsRoute(devices: AlleyDeviceMapper) {
    suspend fun getPlugs() = getStats(devices, { plug: PlugDevice ->
        val power = plug.getPower()

        PlugResponse(
            plug.config.host,
            plug.getName() ?: "",
            if (plug.getPowerState() == true) 1 else 0,
            power.power,
            power.voltage,
            power.current,
            plug.getUptime() ?: -1,
            plug.getSignalStrength() ?: -1
        )
    })

    get<StatsRoute.Plug> {
        call.respond(getPlugs())
    }

    get<StatsRoute.Bulb> {
        getStats(devices, { bulb: BulbDevice ->
            val power = bulb.getPowerUsage()

            BulbResponse(
                bulb.config.host,
                bulb.getName(),
                if (bulb.getPowerState()) 1 else 0,
                power,
                bulb.getSignalStrength()
            )
        }).let {
            call.respond(it)
        }
    }

    get<StatsRoute.Relay> {
        getStats(devices, { relay: RelayDevice ->
            RelayResponse(
                relay.config.host,
                if (relay.getPowerState()) 1 else 0,
                relay.props
            )
        }).let {
            call.respond(it)
        }
    }

    get<StatsRoute.Tado> {
        getStats(devices, { tado: TadoDevice ->
            if (!tado.config.updateReadings) {
                val zones = tado.getHome().getZoneStates()

                TadoResponse(
                    tado.getHomeId(),
                    zones.zoneStates.map { zone ->
                        TransformedZoneState(
                            zone.key,
                            zone.value.tadoMode.ordinal,
                            zone.value.setting,
                            zone.value.activityDataPoints,
                            zone.value.sensorDataPoints
                        )
                    }
                )
            } else {
                null
            }
        }).let {
            call.respond(it.filterNotNull().first())
        }
    }

    get<StatsRoute.TadoMulti> {
        getStats(devices, { tado: TadoDevice ->
            val zones = tado.getHome().getZoneStates()

            TadoResponse(
                tado.getHomeId(),
                zones.zoneStates.map { zone ->
                    TransformedZoneState(
                        zone.key,
                        zone.value.tadoMode.ordinal,
                        zone.value.setting,
                        zone.value.activityDataPoints,
                        zone.value.sensorDataPoints
                    )
                }
            )
        }).let {
            call.respond(it)
        }
    }
}
