package uk.co.thomasc.thealley.devices.zigbee.relay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZMultiRelaySet(
    @SerialName("state_l1")
    val state1: ZRelayAction? = null,
    @SerialName("state_l2")
    val state2: ZRelayAction? = null
)
