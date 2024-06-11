package uk.co.thomasc.thealley.devices.somfy

import kotlinx.serialization.Serializable

@Serializable
data class SomfyBlindState(
    val position: Int = 0,
    val target: Int? = null
)
