package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.TexecomDevice

class TexecomDeviceConfig(val config: TexecomConfig) : IAlleyDeviceConfig<TexecomDevice, TexecomConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = TexecomDevice(id, config, state, stateStore)
    override fun stateSerializer() = EmptyState.serializer()
}
