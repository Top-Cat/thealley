package uk.co.thomasc.thealley.devicev2.energy.tado

import at.topc.tado.Tado
import at.topc.tado.data.eiq.TadoEIQReadingReq
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mu.KLogging
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.energy.bright.BrightEvent
import uk.co.thomasc.thealley.devicev2.types.TadoConfig

class TadoDevice(id: Int, config: TadoConfig, state: TadoState, stateStore: IStateUpdater<TadoState>) :
    AlleyDevice<TadoDevice, TadoConfig, TadoState>(id, config, state, stateStore) {

    val tado = Tado(
        at.topc.tado.config.TadoConfig(
            config.email,
            config.pass
        )
    )

    private val homeId = GlobalScope.async(start = CoroutineStart.LAZY) { tado.me().homes.first().id }
    private val home = GlobalScope.async(start = CoroutineStart.LAZY) { tado.home(homeId.await()) }

    suspend fun getHomeId() = homeId.await()
    suspend fun getHome() = home.await()

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<BrightEvent> {
            val newDate = it.latestReading.toLocalDateTime(TimeZone.UTC).date
            if (state.lastMeterReadDate?.let { d -> newDate > d } != false) {
                home.await().energyIQ().addReading(TadoEIQReadingReq(it.meterTotal.toInt(), newDate))
                updateState(state.copy(lastMeterReadDate = newDate))
            }
        }
    }

    override fun close() {
        tado.close()
    }

    companion object : KLogging()
}
