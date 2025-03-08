package uk.co.thomasc.thealley.devices.state.esphome

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class PS2State(
    val power: Boolean = false
) : IAlleyState
