package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.zigbee.moes.DualSwitchDevice

class DualSwitchDeviceConfig(config: DualSwitchConfig) : IAlleyDeviceConfig<DualSwitchDevice, DualSwitchConfig, EmptyState>(config) {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = DualSwitchDevice(id, config, state, stateStore)
}
