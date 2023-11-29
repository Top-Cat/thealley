package uk.co.thomasc.thealley.devicev2.switch

import uk.co.thomasc.thealley.devicev2.IAlleyEvent

data class SwitchEvent(
    val switchId: Int,
    val buttonId: Int,
    val buttonState: State
) : IAlleyEvent {
    enum class State {
        UNKNOWN, SINGLE, DOUBLE, HOLD, RELEASE;

        companion object {
            private val lookup = entries.associateBy { it.ordinal }
            fun fromInt(i: Int) = lookup[i] ?: UNKNOWN
        }
    }
}
