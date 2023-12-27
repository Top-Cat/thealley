package uk.co.thomasc.thealley.devices.zigbee.moes

import kotlinx.serialization.SerialName

enum class ZigbeePowerOnBehavior {
    @SerialName("on")
    ON,

    @SerialName("off")
    OFF,

    @SerialName("previous")
    PREVIOUS
}
