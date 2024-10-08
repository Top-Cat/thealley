package uk.co.thomasc.thealley.devices.system.conditional

import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.system.conditional.ConditionalState
import uk.co.thomasc.thealley.devices.system.conditional.actions.handler
import uk.co.thomasc.thealley.devices.system.conditional.conditions.ICondition
import uk.co.thomasc.thealley.devices.system.conditional.conditions.handler
import uk.co.thomasc.thealley.devices.types.ConditionalConfig

class ConditionalDevice(id: Int, config: ConditionalConfig, state: ConditionalState, stateStore: IStateUpdater<ConditionalState>, val dev: AlleyDeviceMapper) :
    AlleyDevice<ConditionalDevice, ConditionalConfig, ConditionalState>(id, config, state, stateStore), UpdateCondition {

    override suspend fun init(bus: AlleyEventBusShim) {
        if (state.states.size != config.conditions.size) {
            updateState(state.copy(states = (1..config.conditions.size).map { false }))
        }

        config.conditions.forEach {
            it.handler().setup(this, bus)
        }

        config.trigger.handler().setup(this, bus)
    }

    override suspend fun updateConditionState(condition: ICondition, v: Boolean, bus: AlleyEventEmitter) {
        if (condition == config.trigger && v && state.states.size == config.conditions.size && state.states.all { it }) {
            config.action.handler().perform(dev, bus)
        }

        val idx = config.conditions.indexOf(condition)
        updateState(state.copy(states = state.states.mapIndexed { i, b -> if (i == idx) v else b }))
    }
}
