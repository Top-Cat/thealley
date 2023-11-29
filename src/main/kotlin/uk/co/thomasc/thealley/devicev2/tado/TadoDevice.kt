package uk.co.thomasc.thealley.devicev2.tado

import at.topc.tado.Tado
import at.topc.tado.TadoHomeApi
import mu.KLogging
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.EmptyState
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.types.TadoConfig

class TadoDevice(id: Int, config: TadoConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<TadoDevice, TadoConfig, EmptyState>(id, config, state, stateStore) {

    val tado = Tado(
        at.topc.tado.config.TadoConfig(
            config.email,
            config.pass
        )
    )

    var homeId: Int = 0
    lateinit var home: TadoHomeApi

    override suspend fun init(bus: AlleyEventBus) {
        homeId = tado.me().homes.first().id
        home = tado.home(homeId)
    }

    companion object : KLogging()
}
