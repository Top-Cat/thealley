package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.somfy.SomfyGroupDevice
import uk.co.thomasc.thealley.devices.state.somfy.SomfyGroupState

class SomfyGroupDeviceConfig(config: SomfyGroupConfig) : IAlleyDeviceConfig<SomfyGroupDevice, SomfyGroupConfig, SomfyGroupState>(config) {
    override fun create(id: Int, state: SomfyGroupState, stateStore: IStateUpdater<SomfyGroupState>, dev: AlleyDeviceMapper) = SomfyGroupDevice(id, config, state, stateStore)
}
