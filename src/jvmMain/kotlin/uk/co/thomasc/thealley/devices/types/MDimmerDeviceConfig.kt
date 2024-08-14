package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.zigbee.moes.MDimmerDevice

class MDimmerDeviceConfig(config: MDimmerConfig) : IAlleyDeviceConfig<MDimmerDevice, MDimmerConfig, EmptyState>(config) {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = MDimmerDevice(id, config, state, stateStore)
}
