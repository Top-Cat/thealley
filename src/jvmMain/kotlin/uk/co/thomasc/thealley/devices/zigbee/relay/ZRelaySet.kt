package uk.co.thomasc.thealley.devices.zigbee.relay

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import uk.co.thomasc.thealley.alleyJsonUgly

@Serializable
data class ZRelaySet(val state: ZRelayAction) {
    fun toJson() = alleyJsonUgly.encodeToString(this)
}
