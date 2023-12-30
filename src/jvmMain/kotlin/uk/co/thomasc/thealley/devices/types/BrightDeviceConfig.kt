package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.energy.bright.BrightDevice
import uk.co.thomasc.thealley.devices.energy.bright.BrightState

class BrightDeviceConfig(val config: BrightConfig) : IAlleyDeviceConfig<BrightDevice, BrightConfig, BrightState>() {
    override fun create(id: Int, state: BrightState, stateStore: IStateUpdater<BrightState>, dev: AlleyDeviceMapper) = BrightDevice(id, config, state, stateStore)
    override fun stateSerializer() = BrightState.serializer()
}
