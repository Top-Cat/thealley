package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.EmptyState
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.tado.TadoDevice

@Serializable
@SerialName("Tado")
data class TadoConfig(override val name: String, val email: String, val pass: String) : IAlleyConfig {
    override fun deviceConfig() = TadoDeviceConfig(this)

    class TadoDeviceConfig(val config: TadoConfig) : IAlleyDeviceConfig<TadoDevice, TadoConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = TadoDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
