package uk.co.thomasc.thealley.devicev2.energy.bright.client

import kotlinx.serialization.Serializable

@Serializable
data class BrightCatchupResult(
    val status: String,
    val valid: Boolean
)
