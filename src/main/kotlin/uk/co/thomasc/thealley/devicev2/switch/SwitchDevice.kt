package uk.co.thomasc.thealley.devicev2.switch

import mu.KLogging
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.system.scene.SceneEvent
import uk.co.thomasc.thealley.devicev2.types.SwitchConfig

class SwitchDevice(id: Int, config: SwitchConfig, state: SwitchState, stateStore: IStateUpdater<SwitchState>) :
    AlleyDevice<SwitchDevice, SwitchConfig, SwitchState>(id, config, state, stateStore) {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<SwitchEvent> { ev ->
            if (ev.switchId != config.id) return@handle
            val sceneId = config.scenes[ev.buttonId]

            when (ev.buttonState) {
                SwitchEvent.State.SINGLE -> bus.emit(SceneEvent(sceneId, SceneEvent.Action.TOGGLE))
                SwitchEvent.State.DOUBLE -> bus.emit(SceneEvent(sceneId, SceneEvent.Action.REVOKE))
                SwitchEvent.State.HOLD -> bus.emit(SceneEvent(sceneId, SceneEvent.Action.START_FADE))
                SwitchEvent.State.RELEASE -> bus.emit(SceneEvent(sceneId, SceneEvent.Action.END_FADE))
                else -> logger.warn { "Unknown state ${ev.buttonState}" }
            }
        }
    }

    companion object : KLogging()
}
