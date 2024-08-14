package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.zigbee.aq2.MotionDevice

class MotionDeviceConfig(config: MotionConfig) : IAlleyDeviceConfig<MotionDevice, MotionConfig, EmptyState>(config) {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = MotionDevice(id, config, state, stateStore, dev)
}
