package uk.co.thomasc.thealley.devicev2.scene

import uk.co.thomasc.thealley.devicev2.IAlleyEvent

data class SceneEvent(val scene: Int, val action: Action) : IAlleyEvent {
    enum class Action {
        TOGGLE, REVOKE, START_FADE, END_FADE
    }
}
