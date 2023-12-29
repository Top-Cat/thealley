package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.relay.PartialLightDevice

@Serializable
@SerialName("PartialLight")
data class PartialLightConfig(
    override val name: String,
    val device: Int,
    val index: Int
) : IAlleyConfig {
    override fun deviceConfig() = PartialLightDeviceConfig(this)

    class PartialLightDeviceConfig(val config: PartialLightConfig) : IAlleyDeviceConfig<PartialLightDevice, PartialLightConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = PartialLightDevice(id, config, state, stateStore, dev)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
