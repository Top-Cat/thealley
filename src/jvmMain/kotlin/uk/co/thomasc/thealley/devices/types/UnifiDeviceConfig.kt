package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.unifi.UnifiDevice

class UnifiDeviceConfig(val config: UnifiConfig) : IAlleyDeviceConfig<UnifiDevice, UnifiConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = UnifiDevice(id, config, state, stateStore)
    override fun stateSerializer() = EmptyState.serializer()
}
