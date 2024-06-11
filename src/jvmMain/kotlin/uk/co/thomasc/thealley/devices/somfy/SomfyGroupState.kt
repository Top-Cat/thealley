package uk.co.thomasc.thealley.devices.somfy

import kotlinx.serialization.Serializable

@Serializable
data class SomfyGroupState(
    val position: Boolean = false
)
