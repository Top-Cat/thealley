package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.kasa.plug.PlugDevice
import uk.co.thomasc.thealley.devices.state.kasa.plug.PlugState

class PlugDeviceConfig(config: PlugConfig) : IAlleyDeviceConfig<PlugDevice, PlugConfig, PlugState>(config) {
    override fun create(id: Int, state: PlugState, stateStore: IStateUpdater<PlugState>, dev: AlleyDeviceMapper) = PlugDevice(id, config, state, stateStore)
}
