package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.switch.SwitchServerDevice

@Serializable
@SerialName("SwitchServer")
data class SwitchServerConfig(override val name: String, val port: Int) : IAlleyConfig {
    override fun deviceConfig() = SwitchServerDeviceConfig(this)

    class SwitchServerDeviceConfig(val config: SwitchServerConfig) : IAlleyDeviceConfig<SwitchServerDevice, SwitchServerConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = SwitchServerDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
