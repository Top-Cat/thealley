package uk.co.thomasc.thealley.devices.zigbee.samotech

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction

@Serializable
data class ZDimmerSet(
    val brightness: Int,
    val state: ZRelayAction? = null,
    val transition: Float? = null
)
