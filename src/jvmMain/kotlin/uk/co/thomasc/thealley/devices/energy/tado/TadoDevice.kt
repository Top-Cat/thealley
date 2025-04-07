package uk.co.thomasc.thealley.devices.energy.tado

import at.topc.tado.Tado
import at.topc.tado.data.eiq.TadoEIQReadingReq
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.energy.bright.BrightEvent
import uk.co.thomasc.thealley.devices.state.energy.tado.TadoState
import uk.co.thomasc.thealley.devices.types.TadoConfig

class TadoDevice(id: Int, config: TadoConfig, state: TadoState, stateStore: IStateUpdater<TadoState>) :
    AlleyDevice<TadoDevice, TadoConfig, TadoState>(id, config, state, stateStore) {

    private val tado = Tado(
        at.topc.tado.config.TadoConfig(
            config.email,
            state.refreshToken,
            persistRefreshToken = { token ->
                updateState {
                    state.copy(
                        refreshToken = token
                    )
                }
            }
        )
    )

    private val homeId = GlobalScope.async(start = CoroutineStart.LAZY) { tado.me().homes.first().id }
    private val home = GlobalScope.async(start = CoroutineStart.LAZY) { tado.home(getHomeId()) }

    suspend fun getHomeId() = homeId.await()
    suspend fun getHome() = home.await()

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<BrightEvent> {
            if (!config.updateReadings) return@handle

            val newDate = it.latestReading.toLocalDateTime(TimeZone.UTC).date
            if (state.lastMeterReadDate?.let { d -> newDate > d } != false) {
                getHome().energyIQ().addReading(TadoEIQReadingReq(it.meterTotal.toInt(), newDate))
                updateState(state.copy(lastMeterReadDate = newDate))
            }
        }
    }

    override fun close() {
        tado.close()
    }

    companion object : KLogging()
}
