package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.conditional.ConditionalDevice
import uk.co.thomasc.thealley.devices.system.conditional.ConditionalState

class ConditionalDeviceConfig(val config: ConditionalConfig) : IAlleyDeviceConfig<ConditionalDevice, ConditionalConfig, ConditionalState>() {
    override fun create(id: Int, state: ConditionalState, stateStore: IStateUpdater<ConditionalState>, dev: AlleyDeviceMapper) = ConditionalDevice(id, config, state, stateStore, dev)
    override fun stateSerializer() = ConditionalState.serializer()
}
