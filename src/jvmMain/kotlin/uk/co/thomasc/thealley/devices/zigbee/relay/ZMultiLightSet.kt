package uk.co.thomasc.thealley.devices.zigbee.relay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZMultiLightSet(
    @SerialName("brightness_l1")
    val brightness1: Int? = null,
    @SerialName("brightness_l2")
    val brightness2: Int? = null
)
