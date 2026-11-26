package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.wiim.WiimEvent

class WiimConditionHandler(condition: WiimCondition) : Condition<WiimCondition>(condition) {
    override suspend fun setupHandler(bus: AlleyEventBusShim) {
        bus.handle<WiimEvent> { ev ->
            if (ev.uuid != condition.uuid) return@handle
            updateCondition(ev.state == condition.state)
        }
    }
}
