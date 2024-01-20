package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.alarm.TexecomAreaEvent

class TexecomAreaConditionHandler(condition: TexecomAreaCondition) : Condition<TexecomAreaCondition>(condition) {
    override suspend fun setupHandler(bus: AlleyEventBus) {
        bus.handle<TexecomAreaEvent> { ev ->
            if (ev.areaId != condition.areaId) return@handle
            updateCondition(ev.status == condition.areaState)
        }
    }
}
