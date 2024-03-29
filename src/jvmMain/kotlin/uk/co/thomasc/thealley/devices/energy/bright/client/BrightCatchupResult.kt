package uk.co.thomasc.thealley.devices.energy.bright.client

import kotlinx.serialization.Serializable

@Serializable
data class BrightCatchupResult(
    val status: String? = null,
    val valid: Boolean
)
