package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.system.sun.SunState
import uk.co.thomasc.thealley.devices.system.sun.SunDevice

class SunDeviceConfig(config: SunConfig) : IAlleyDeviceConfig<SunDevice, SunConfig, SunState>(config) {
    override fun create(id: Int, state: SunState, stateStore: IStateUpdater<SunState>, dev: AlleyDeviceMapper) = SunDevice(id, config, state, stateStore)
}
