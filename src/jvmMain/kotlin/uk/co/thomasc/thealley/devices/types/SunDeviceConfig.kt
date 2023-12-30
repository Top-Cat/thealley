package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.sun.SunDevice
import uk.co.thomasc.thealley.devices.system.sun.SunState

class SunDeviceConfig(val config: SunConfig) : IAlleyDeviceConfig<SunDevice, SunConfig, SunState>() {
    override fun create(id: Int, state: SunState, stateStore: IStateUpdater<SunState>, dev: AlleyDeviceMapper) = SunDevice(id, config, state, stateStore)
    override fun stateSerializer() = SunState.serializer()
}
