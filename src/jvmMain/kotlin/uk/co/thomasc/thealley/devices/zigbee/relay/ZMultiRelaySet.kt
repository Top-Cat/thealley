package uk.co.thomasc.thealley.devices.zigbee.relay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import uk.co.thomasc.thealley.alleyJsonUgly

@Serializable
data class ZMultiRelaySet(
    @SerialName("state_l1")
    val state1: ZRelayAction? = null,
    @SerialName("state_l2")
    val state2: ZRelayAction? = null
) {
    fun toJson() = alleyJsonUgly.encodeToString(this)
}
