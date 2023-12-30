package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.moes.DualDimmerDevice

class DualDimmerDeviceConfig(val config: DualDimmerConfig) : IAlleyDeviceConfig<DualDimmerDevice, DualDimmerConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = DualDimmerDevice(id, config, state, stateStore)
    override fun stateSerializer() = EmptyState.serializer()
}
