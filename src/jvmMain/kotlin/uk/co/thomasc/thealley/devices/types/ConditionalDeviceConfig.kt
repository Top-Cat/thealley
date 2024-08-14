package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.system.conditional.ConditionalState
import uk.co.thomasc.thealley.devices.system.conditional.ConditionalDevice

class ConditionalDeviceConfig(config: ConditionalConfig) : IAlleyDeviceConfig<ConditionalDevice, ConditionalConfig, ConditionalState>(config) {
    override fun create(id: Int, state: ConditionalState, stateStore: IStateUpdater<ConditionalState>, dev: AlleyDeviceMapper) = ConditionalDevice(id, config, state, stateStore, dev)
}
