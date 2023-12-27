package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.moes.DualSwitchDevice

@Serializable
@SerialName("DualSwitch")
data class DualSwitchConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig, IZigbeeConfig {
    override fun deviceConfig() = DualSwitchDeviceConfig(this)

    class DualSwitchDeviceConfig(val config: DualSwitchConfig) : IAlleyDeviceConfig<DualSwitchDevice, DualSwitchConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = DualSwitchDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
