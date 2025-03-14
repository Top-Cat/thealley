package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.system.conditional.UpdateCondition

abstract class Condition<T : ICondition>(protected val condition: T) {
    private lateinit var device: UpdateCondition
    private lateinit var bus: AlleyEventBusShim

    suspend fun setup(device: UpdateCondition, bus: AlleyEventBusShim) {
        this.device = device
        this.bus = bus
        setupHandler(bus)
    }

    abstract suspend fun setupHandler(bus: AlleyEventBusShim)

    suspend fun updateCondition(state: Boolean) = device.updateConditionState(condition, state, bus)
}

fun ICondition.handler() = when (this) {
    is RelayCondition -> RelayConditionHandler(this)
    is LuxCondition -> LuxConditionHandler(this)
    is SunCondition -> SunConditionHandler(this)
    is TexecomAreaCondition -> TexecomAreaConditionHandler(this)
    is TexecomZoneCondition -> TexecomZoneConditionHandler(this)
}
