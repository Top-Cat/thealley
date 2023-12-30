package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.aq2.MotionDevice

class MotionDeviceConfig(val config: MotionConfig) : IAlleyDeviceConfig<MotionDevice, MotionConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = MotionDevice(id, config, state, stateStore, dev)
    override fun stateSerializer() = EmptyState.serializer()
}
