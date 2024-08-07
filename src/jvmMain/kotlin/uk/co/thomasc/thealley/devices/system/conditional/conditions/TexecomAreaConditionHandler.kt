package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.alarm.events.TexecomAreaEvent

class TexecomAreaConditionHandler(condition: TexecomAreaCondition) : Condition<TexecomAreaCondition>(condition) {
    override suspend fun setupHandler(bus: AlleyEventBusShim) {
        bus.handle<TexecomAreaEvent> { ev ->
            if (ev.areaId != condition.areaId) return@handle
            updateCondition(ev.status == condition.areaState)
        }
    }
}
