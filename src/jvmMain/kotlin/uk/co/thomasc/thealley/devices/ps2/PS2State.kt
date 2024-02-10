package uk.co.thomasc.thealley.devices.ps2

import kotlinx.serialization.Serializable

@Serializable
data class PS2State(
    val power: Boolean = false
)
