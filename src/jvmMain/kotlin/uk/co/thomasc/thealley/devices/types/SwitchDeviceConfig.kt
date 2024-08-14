package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.switch.SwitchState
import uk.co.thomasc.thealley.devices.switch.SwitchDevice

class SwitchDeviceConfig(config: SwitchConfig) : IAlleyDeviceConfig<SwitchDevice, SwitchConfig, SwitchState>(config) {
    override fun create(id: Int, state: SwitchState, stateStore: IStateUpdater<SwitchState>, dev: AlleyDeviceMapper) = SwitchDevice(id, config, state, stateStore)
}
