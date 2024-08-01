package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.onkyo.RelayStateEvent

class RelayConditionHandler(condition: RelayCondition) : Condition<RelayCondition>(condition) {
    override suspend fun setupHandler(bus: AlleyEventBusShim) {
        bus.handle<RelayStateEvent> { ev ->
            if (ev.deviceId != condition.deviceId) return@handle
            updateCondition(ev.state == condition.state)
        }
    }
}
