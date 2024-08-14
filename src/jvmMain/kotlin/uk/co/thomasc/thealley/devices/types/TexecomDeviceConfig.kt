package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.TexecomDevice
import uk.co.thomasc.thealley.devices.state.alarm.TexecomState

class TexecomDeviceConfig(config: TexecomConfig) : IAlleyDeviceConfig<TexecomDevice, TexecomConfig, TexecomState>(config) {
    override fun create(id: Int, state: TexecomState, stateStore: IStateUpdater<TexecomState>, dev: AlleyDeviceMapper) = TexecomDevice(id, config, state, stateStore)
}
