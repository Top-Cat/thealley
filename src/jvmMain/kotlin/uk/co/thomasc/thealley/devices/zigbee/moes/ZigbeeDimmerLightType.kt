package uk.co.thomasc.thealley.devices.zigbee.moes

import kotlinx.serialization.SerialName

enum class ZigbeeDimmerLightType {
    @SerialName("led")
    LED,

    @SerialName("incandescent")
    INCANDESCENT,

    @SerialName("halogen")
    HALOGEN
}
