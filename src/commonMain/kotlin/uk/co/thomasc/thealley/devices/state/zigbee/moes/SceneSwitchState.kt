package uk.co.thomasc.thealley.devices.state.zigbee.moes

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.zigbee.IZigbeeState

@Serializable
data class SceneSwitchState(val state: Map<Int, Boolean> = mapOf()) : IZigbeeState
