package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.energy.tado.TadoDevice
import uk.co.thomasc.thealley.devices.energy.tado.TadoState

@Serializable
@SerialName("Tado")
data class TadoConfig(override val name: String, val email: String, val pass: String, val updateReadings: Boolean = false) : IAlleyConfig {
    override fun deviceConfig() = TadoDeviceConfig(this)

    class TadoDeviceConfig(val config: TadoConfig) : IAlleyDeviceConfig<TadoDevice, TadoConfig, TadoState>() {
        override fun create(id: Int, state: TadoState, stateStore: IStateUpdater<TadoState>, dev: AlleyDeviceMapper) = TadoDevice(id, config, state, stateStore)
        override fun stateSerializer() = TadoState.serializer()
    }
}
