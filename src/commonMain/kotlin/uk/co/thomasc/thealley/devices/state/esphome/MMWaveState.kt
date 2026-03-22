package uk.co.thomasc.thealley.devices.state.esphome

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.zigbee.IZigbeeState

@Serializable
data class MMWaveState(
    val lightIntensity: Int = 0,
    val occupied: Boolean = false
) : IZigbeeState
