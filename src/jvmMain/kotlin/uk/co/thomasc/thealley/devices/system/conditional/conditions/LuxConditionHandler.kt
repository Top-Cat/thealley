package uk.co.thomasc.thealley.devices.system.conditional.conditions

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.esphome.LuxEvent

class LuxConditionHandler(condition: LuxCondition) : Condition<LuxCondition>(condition) {
    override suspend fun setupHandler(bus: AlleyEventBusShim) {
        bus.handle<LuxEvent> { ev ->
            if (ev.deviceId != condition.deviceId) return@handle
            updateCondition((if (condition.lux >= 0) ev.lux > condition.lux else ev.on()) xor !condition.above)
        }
    }
}
