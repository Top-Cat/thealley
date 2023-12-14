package uk.co.thomasc.thealley.devices.zigbee.blind

import kotlinx.serialization.Serializable

@Serializable
data class BlindState(val position: Int? = null)
