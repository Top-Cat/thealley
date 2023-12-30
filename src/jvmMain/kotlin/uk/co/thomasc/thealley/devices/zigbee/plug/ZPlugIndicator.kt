package uk.co.thomasc.thealley.devices.zigbee.plug

import kotlinx.serialization.SerialName

enum class ZPlugIndicator {
    @SerialName("off")
    BLUE,

    @SerialName("off/on")
    BLUE_PURPLE,

    @SerialName("on/off")
    PURPLE_BLUE,

    @SerialName("on")
    PURPLE
}
