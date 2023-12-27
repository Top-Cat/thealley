package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.relay.PartialRelayDevice

@Serializable
@SerialName("PartialRelay")
data class PartialRelayConfig(
    override val name: String,
    val device: Int,
    val index: Int
) : IAlleyConfig {
    override fun deviceConfig() = PartialRelayDeviceConfig(this)

    class PartialRelayDeviceConfig(val config: PartialRelayConfig) : IAlleyDeviceConfig<PartialRelayDevice, PartialRelayConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = PartialRelayDevice(id, config, state, stateStore, dev)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
