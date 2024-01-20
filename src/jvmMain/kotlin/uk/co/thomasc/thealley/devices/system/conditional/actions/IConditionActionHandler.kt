package uk.co.thomasc.thealley.devices.system.conditional.actions

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus

interface IConditionActionHandler {
    suspend fun perform(dev: AlleyDeviceMapper, bus: AlleyEventBus)
}

fun IConditionAction.handler() = when (this) {
    is SceneConditionAction -> SceneConditionActionHandler(this)
}

