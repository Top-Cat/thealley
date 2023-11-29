package uk.co.thomasc.thealley.devicev2.scene

import kotlinx.serialization.Serializable

@Serializable
data class SceneState(val fadeStarted: Long, val startBrightness: Int, val direction: Boolean)
