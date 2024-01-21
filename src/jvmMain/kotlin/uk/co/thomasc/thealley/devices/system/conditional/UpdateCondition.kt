package uk.co.thomasc.thealley.devices.system.conditional

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.system.conditional.conditions.ICondition

interface UpdateCondition {
    suspend fun updateConditionState(condition: ICondition, v: Boolean, bus: AlleyEventBus)
}
