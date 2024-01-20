package uk.co.thomasc.thealley.devices.system.conditional.actions

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.system.scene.SceneDevice

class SceneConditionActionHandler(val action: SceneConditionAction) : IConditionActionHandler {
    override suspend fun perform(dev: AlleyDeviceMapper, bus: AlleyEventBus) {
        dev.getDevice<SceneDevice>(action.sceneId)?.execute(bus)
    }
}