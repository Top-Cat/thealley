package uk.co.thomasc.thealley.devices.system.scene

import kotlinx.serialization.Serializable

@Serializable
data class SceneState(
    val fadeStarted: Long = 0,
    val startBrightness: Int = 0,
    val direction: Boolean = false
)
