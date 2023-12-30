package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.switch.SwitchDevice
import uk.co.thomasc.thealley.devices.switch.SwitchState

class SwitchDeviceConfig(val config: SwitchConfig) : IAlleyDeviceConfig<SwitchDevice, SwitchConfig, SwitchState>() {
    override fun create(id: Int, state: SwitchState, stateStore: IStateUpdater<SwitchState>, dev: AlleyDeviceMapper) = SwitchDevice(id, config, state, stateStore)
    override fun stateSerializer() = SwitchState.serializer()
}
