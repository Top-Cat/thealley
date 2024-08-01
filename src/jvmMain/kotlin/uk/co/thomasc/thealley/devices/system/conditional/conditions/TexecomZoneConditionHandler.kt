package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.alarm.events.TexecomZoneEvent

class TexecomZoneConditionHandler(condition: TexecomZoneCondition) : Condition<TexecomZoneCondition>(condition) {
    override suspend fun setupHandler(bus: AlleyEventBusShim) {
        bus.handle<TexecomZoneEvent> { ev ->
            if (ev.zoneId != condition.zoneId) return@handle
            updateCondition(ev.status == condition.zoneState)
        }
    }
}
