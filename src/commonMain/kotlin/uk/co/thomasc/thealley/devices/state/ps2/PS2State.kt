package uk.co.thomasc.thealley.devices.state.ps2

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class PS2State(
    val power: Boolean = false
) : IAlleyState
