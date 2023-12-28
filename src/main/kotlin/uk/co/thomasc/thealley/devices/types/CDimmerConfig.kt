package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.samotech.CDimmerDevice

@Serializable
@SerialName("CDimmer")
data class CDimmerConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig, IZigbeeConfig {
    override fun deviceConfig() = CDimmerDeviceConfig(this)

    class CDimmerDeviceConfig(val config: CDimmerConfig) : IAlleyDeviceConfig<CDimmerDevice, CDimmerConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = CDimmerDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
