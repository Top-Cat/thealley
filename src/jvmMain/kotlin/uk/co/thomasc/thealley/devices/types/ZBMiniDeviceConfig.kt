package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.zigbee.zbmini.ZBMiniDevice

class ZBMiniDeviceConfig(config: ZBMiniConfig) : IAlleyDeviceConfig<ZBMiniDevice, ZBMiniConfig, EmptyState>(config) {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = ZBMiniDevice(id, config, state, stateStore)
}
