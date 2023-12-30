package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.relay.PartialLightDevice

class PartialLightDeviceConfig(val config: PartialLightConfig) : IAlleyDeviceConfig<PartialLightDevice, PartialLightConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = PartialLightDevice(id, config, state, stateStore, dev)
    override fun stateSerializer() = EmptyState.serializer()
}
