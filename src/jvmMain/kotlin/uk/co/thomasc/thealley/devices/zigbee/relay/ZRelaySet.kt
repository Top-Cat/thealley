package uk.co.thomasc.thealley.devices.zigbee.relay

import kotlinx.serialization.Serializable

@Serializable
data class ZRelaySet(val state: ZRelayAction)
