package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.energy.tado.TadoDevice
import uk.co.thomasc.thealley.devicev2.energy.tado.TadoState

@Serializable
@SerialName("Tado")
data class TadoConfig(override val name: String, val email: String, val pass: String) : IAlleyConfig {
    override fun deviceConfig() = TadoDeviceConfig(this)

    class TadoDeviceConfig(val config: TadoConfig) : IAlleyDeviceConfig<TadoDevice, TadoConfig, TadoState>() {
        override fun create(id: Int, state: TadoState, stateStore: IStateUpdater<TadoState>, dev: AlleyDeviceMapper) = TadoDevice(id, config, state, stateStore)
        override fun stateSerializer() = TadoState.serializer()
    }
}
