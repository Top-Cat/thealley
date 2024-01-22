package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.candeo.CDimmerDevice

class CDimmerDeviceConfig(val config: CDimmerConfig) : IAlleyDeviceConfig<CDimmerDevice, CDimmerConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = CDimmerDevice(id, config, state, stateStore)
    override fun stateSerializer() = EmptyState.serializer()
}
