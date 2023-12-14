package uk.co.thomasc.thealley.devices.zigbee.plug

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import uk.co.thomasc.thealley.alleyJsonUgly

@Serializable
data class ZPlugSet(val state: ZPlugAction) {
    fun toJson() = alleyJsonUgly.encodeToString(this)
}
