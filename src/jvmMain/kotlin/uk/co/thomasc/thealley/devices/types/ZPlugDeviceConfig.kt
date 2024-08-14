package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.zigbee.plug.ZPlugDevice

class ZPlugDeviceConfig(config: ZPlugConfig) : IAlleyDeviceConfig<ZPlugDevice, ZPlugConfig, EmptyState>(config) {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = ZPlugDevice(id, config, state, stateStore)
}
