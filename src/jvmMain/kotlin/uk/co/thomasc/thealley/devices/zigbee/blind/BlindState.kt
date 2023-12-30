package uk.co.thomasc.thealley.devices.zigbee.blind

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.IZigbeeState

@Serializable
data class BlindState(val position: Int? = null) : IZigbeeState
