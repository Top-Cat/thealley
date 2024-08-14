package uk.co.thomasc.thealley.devices.system.scene

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class SceneState(
    val fadeStarted: Long = 0,
    val startBrightness: Int = 0,
    val direction: Boolean = false
) : IAlleyState
