package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.somfy.SomfyBlindDevice
import uk.co.thomasc.thealley.devices.somfy.SomfyBlindState

class SomfyBlindDeviceConfig(val config: SomfyBlindConfig) : IAlleyDeviceConfig<SomfyBlindDevice, SomfyBlindConfig, SomfyBlindState>() {
    override fun create(id: Int, state: SomfyBlindState, stateStore: IStateUpdater<SomfyBlindState>, dev: AlleyDeviceMapper) = SomfyBlindDevice(id, config, state, stateStore)
    override fun stateSerializer() = SomfyBlindState.serializer()
}
