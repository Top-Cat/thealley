package uk.co.thomasc.thealley.devices.zigbee.plug

import kotlinx.serialization.SerialName

enum class ZigbeePOM {
    @SerialName("restore")
    RESTORE,

    @SerialName("on")
    ON,

    @SerialName("off")
    OFF
}
