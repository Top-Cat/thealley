package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.samotech.SDimmerDevice

class SDimmerDeviceConfig(val config: SDimmerConfig) : IAlleyDeviceConfig<SDimmerDevice, SDimmerConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = SDimmerDevice(id, config, state, stateStore)
    override fun stateSerializer() = EmptyState.serializer()
}
