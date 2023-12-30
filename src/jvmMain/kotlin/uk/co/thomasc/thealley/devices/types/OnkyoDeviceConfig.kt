package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.onkyo.OnkyoDevice

class OnkyoDeviceConfig(val config: OnkyoConfig) : IAlleyDeviceConfig<OnkyoDevice, OnkyoConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = OnkyoDevice(id, config, state, stateStore)
    override fun stateSerializer() = EmptyState.serializer()
}
