package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.samotech.SDimmerDevice

@Serializable
@SerialName("SDimmer")
data class SDimmerConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig, IZigbeeConfig {
    override fun deviceConfig() = SDimmerDeviceConfig(this)

    class SDimmerDeviceConfig(val config: SDimmerConfig) : IAlleyDeviceConfig<SDimmerDevice, SDimmerConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = SDimmerDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
