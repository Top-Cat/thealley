package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.somfy.SomfyBlindDevice
import uk.co.thomasc.thealley.devices.state.somfy.SomfyBlindState

class SomfyBlindDeviceConfig(config: SomfyBlindConfig) : IAlleyDeviceConfig<SomfyBlindDevice, SomfyBlindConfig, SomfyBlindState>(config) {
    override fun create(id: Int, state: SomfyBlindState, stateStore: IStateUpdater<SomfyBlindState>, dev: AlleyDeviceMapper) = SomfyBlindDevice(id, config, state, stateStore)
}
