package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.alarm.TexecomZoneEvent

class TexecomZoneConditionHandler(condition: TexecomZoneCondition) : Condition<TexecomZoneCondition>(condition) {
    override suspend fun setupHandler(bus: AlleyEventBus) {
        bus.handle<TexecomZoneEvent> { ev ->
            if (ev.zoneId != condition.zoneId) return@handle
            updateCondition(ev.status == condition.zoneState)
        }
    }
}
