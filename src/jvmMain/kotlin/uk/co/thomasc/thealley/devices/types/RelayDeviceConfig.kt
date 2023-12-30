package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.relay.RelayDevice
import uk.co.thomasc.thealley.devices.relay.RelayState

class RelayDeviceConfig(val config: RelayConfig) : IAlleyDeviceConfig<RelayDevice, RelayConfig, RelayState>() {
    override fun create(id: Int, state: RelayState, stateStore: IStateUpdater<RelayState>, dev: AlleyDeviceMapper) = RelayDevice(id, config, state, stateStore)
    override fun stateSerializer() = RelayState.serializer()
}
