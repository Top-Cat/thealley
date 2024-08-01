package uk.co.thomasc.thealley.devices.system.conditional.actions

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventEmitter

interface IConditionActionHandler {
    suspend fun perform(dev: AlleyDeviceMapper, bus: AlleyEventEmitter)
}

fun IConditionAction.handler() = when (this) {
    is SceneConditionAction -> SceneConditionActionHandler(this)
    is RelayConditionAction -> RelayConditionActionHandler(this)
}
