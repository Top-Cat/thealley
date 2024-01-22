package uk.co.thomasc.thealley.devices.system.scene

import uk.co.thomasc.thealley.devices.system.IAlleyEvent

data class SceneEvent(val scene: Int, val action: Action) : IAlleyEvent {
    enum class Action {
        TOGGLE, REVOKE, START_FADE, END_FADE
    }
}
