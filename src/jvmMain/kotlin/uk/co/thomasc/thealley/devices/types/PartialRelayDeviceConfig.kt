package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.zigbee.relay.PartialRelayDevice

class PartialRelayDeviceConfig(config: PartialRelayConfig) : IAlleyDeviceConfig<PartialRelayDevice, PartialRelayConfig, EmptyState>(config) {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = PartialRelayDevice(id, config, state, stateStore, dev)
}
