package uk.co.thomasc.thealley.devices.state.somfy

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class SomfyGroupState(
    val position: Boolean = false
) : IAlleyState
