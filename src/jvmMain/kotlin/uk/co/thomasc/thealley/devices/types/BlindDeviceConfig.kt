package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.zigbee.blind.BlindState
import uk.co.thomasc.thealley.devices.zigbee.blind.BlindDevice

class BlindDeviceConfig(config: BlindConfig) : IAlleyDeviceConfig<BlindDevice, BlindConfig, BlindState>(config) {
    override fun create(id: Int, state: BlindState, stateStore: IStateUpdater<BlindState>, dev: AlleyDeviceMapper) = BlindDevice(id, config, state, stateStore)
}
