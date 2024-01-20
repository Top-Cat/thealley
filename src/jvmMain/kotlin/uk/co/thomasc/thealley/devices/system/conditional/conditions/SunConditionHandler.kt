package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.system.sun.SunRiseEvent
import uk.co.thomasc.thealley.devices.system.sun.SunSetEvent

class SunConditionHandler(condition: SunCondition) : Condition<SunCondition>(condition) {
    override suspend fun setupHandler(bus: AlleyEventBus) {
        bus.handle<SunRiseEvent> {
            updateCondition(condition.daytime)
        }

        bus.handle<SunSetEvent> {
            updateCondition(!condition.daytime)
        }
    }
}
