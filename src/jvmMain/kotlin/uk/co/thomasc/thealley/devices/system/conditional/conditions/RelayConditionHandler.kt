package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.onkyo.RelayStateEvent
import uk.co.thomasc.thealley.devices.system.TickEvent
import kotlin.time.Duration.Companion.seconds

class RelayConditionHandler(condition: RelayCondition) : Condition<RelayCondition>(condition) {
    private var triggerAt = Instant.DISTANT_FUTURE

    override suspend fun setupHandler(bus: AlleyEventBusShim) {
        bus.handle<RelayStateEvent> { ev ->
            if (ev.deviceId != condition.deviceId) return@handle

            val newState = ev.state == condition.state

            triggerAt = if (newState && condition.period > 0) {
                Clock.System.now().plus(condition.period.seconds)
            } else {
                updateCondition(ev.state == condition.state)
                Instant.DISTANT_FUTURE
            }
        }

        bus.handle<TickEvent> { ev ->
            if (triggerAt < ev.now) updateCondition(true)
        }
    }
}
