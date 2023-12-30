package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.switch.SwitchServerDevice

class SwitchServerDeviceConfig(val config: SwitchServerConfig) : IAlleyDeviceConfig<SwitchServerDevice, SwitchServerConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = SwitchServerDevice(id, config, state, stateStore)
    override fun stateSerializer() = EmptyState.serializer()
}
