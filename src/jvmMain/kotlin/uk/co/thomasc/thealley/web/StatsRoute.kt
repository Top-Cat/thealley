package uk.co.thomasc.thealley.web

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.energy.tado.TadoDevice
import uk.co.thomasc.thealley.devices.kasa.bulb.BulbDevice
import uk.co.thomasc.thealley.devices.kasa.plug.PlugDevice
import uk.co.thomasc.thealley.devices.relay.RelayDevice
import uk.co.thomasc.thealley.web.stats.BulbResponse
import uk.co.thomasc.thealley.web.stats.PlugResponse
import uk.co.thomasc.thealley.web.stats.RelayResponse
import uk.co.thomasc.thealley.web.stats.TransformedZoneState

@Location("/stats")
class StatsRoute : IAlleyRoute {
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

    private suspend inline fun <reified T : AlleyDevice<*, *, *>, U> getStats(devices: AlleyDeviceMapper, crossinline block: suspend (T) -> U, concurrency: Int = 10) =
        devices.getDevices<T>().asFlow().flatMapMerge(concurrency) { device ->
            flow {
                emit(
                    block(device)
                )
            }
        }.toList()

    private suspend fun getPlugs(devices: AlleyDeviceMapper) = getStats(devices, { plug: PlugDevice ->
        val power = plug.getPower()

        PlugResponse(
            plug.config.host,
            plug.getName() ?: "",
            if (plug.getPowerState()) 1 else 0,
            power.power,
            power.voltage,
            power.current,
            plug.getUptime() ?: -1,
            plug.getSignalStrength() ?: -1
        )
    })

    override fun Route.setup(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper) {
        get<Plug> {
            call.respond(getPlugs(deviceMapper))
        }

        get<Bulb> {
            getStats(deviceMapper, { bulb: BulbDevice ->
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

        get<Relay> {
            getStats(deviceMapper, { relay: RelayDevice ->
                RelayResponse(
                    relay.config.host,
                    if (relay.getPowerState()) 1 else 0,
                    relay.props
                )
            }).let {
                call.respond(it)
            }
        }

        suspend fun mapHome(tado: TadoDevice): List<TransformedZoneState> {
            val home = tado.getHome()
            val zones = tado.getHome().getZones().associateBy { it.id }
            val zoneStates = home.getZoneStates().zoneStates

            return zones.map { zone ->
                val state = zoneStates[zone.key]
                TransformedZoneState(
                    tado.getHomeId().toString(),
                    zone.key,
                    zone.value.name,
                    zone.value.devices.size,
                    state?.tadoMode?.ordinal ?: 0,
                    state?.setting,
                    state?.activityDataPoints ?: mapOf(),
                    state?.sensorDataPoints ?: mapOf()
                )
            }
        }

        get<Tado> {
            getStats(deviceMapper, { tado: TadoDevice ->
                if (!tado.config.updateReadings) {
                    mapHome(tado)
                } else {
                    null
                }
            }).let {
                call.respond(it.filterNotNull().first())
            }
        }

        get<TadoMulti> {
            getStats(deviceMapper, { tado: TadoDevice ->
                mapHome(tado)
            }).flatten().let {
                call.respond(it)
            }
        }
    }
}
