package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.system.conditional.UpdateCondition

abstract class Condition<T : ICondition>(protected val condition: T) {
    private lateinit var device: UpdateCondition

    suspend fun setup(device: UpdateCondition, bus: AlleyEventBus) {
        this.device = device
        setupHandler(bus)
    }

    abstract suspend fun setupHandler(bus: AlleyEventBus)

    suspend fun updateCondition(state: Boolean) = device.updateConditionState(condition, state)
}

fun ICondition.handler() = when (this) {
    is SunCondition -> SunConditionHandler(this)
    is TexecomAreaCondition -> TexecomAreaConditionHandler(this)
    is TexecomZoneCondition -> TexecomZoneConditionHandler(this)
}
