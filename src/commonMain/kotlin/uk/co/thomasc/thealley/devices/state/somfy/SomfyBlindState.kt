package uk.co.thomasc.thealley.devices.state.somfy

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class SomfyBlindState(
    val position: Int = 0,
    val target: Int? = null
) : IAlleyState
