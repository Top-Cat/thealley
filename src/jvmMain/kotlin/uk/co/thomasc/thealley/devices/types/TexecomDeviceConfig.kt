package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.TexecomDevice
import uk.co.thomasc.thealley.devices.alarm.TexecomState

class TexecomDeviceConfig(val config: TexecomConfig) : IAlleyDeviceConfig<TexecomDevice, TexecomConfig, TexecomState>() {
    override fun create(id: Int, state: TexecomState, stateStore: IStateUpdater<TexecomState>, dev: AlleyDeviceMapper) = TexecomDevice(id, config, state, stateStore)
    override fun stateSerializer() = TexecomState.serializer()
}
