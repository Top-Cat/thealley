package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.onkyo.OnkyoDevice
import uk.co.thomasc.thealley.devices.state.EmptyState

class OnkyoDeviceConfig(config: OnkyoConfig) : IAlleyDeviceConfig<OnkyoDevice, OnkyoConfig, EmptyState>(config) {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = OnkyoDevice(id, config, state, stateStore)
}
