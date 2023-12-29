package uk.co.thomasc.thealley.devices.zigbee.moes

import kotlinx.serialization.SerialName

enum class ZigbeeBacklightMode {
    @SerialName("off")
    OFF,

    @SerialName("normal")
    NORMAL,

    @SerialName("inverted")
    INVERTED
}
