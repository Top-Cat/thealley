package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.switch.SwitchServerDevice

class SwitchServerDeviceConfig(config: SwitchServerConfig) : IAlleyDeviceConfig<SwitchServerDevice, SwitchServerConfig, EmptyState>(config) {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = SwitchServerDevice(id, config, state, stateStore)
}
