package uk.co.thomasc.thealley.devices.state.zigbee.blind

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState
import uk.co.thomasc.thealley.devices.state.zigbee.IZigbeeState

@Serializable
data class BlindState(val position: Int? = null) : IZigbeeState, IAlleyState
