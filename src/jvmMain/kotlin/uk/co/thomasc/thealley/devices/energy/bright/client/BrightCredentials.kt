package uk.co.thomasc.thealley.devices.energy.bright.client

import kotlinx.serialization.Serializable

@Serializable
data class BrightCredentials(val username: String, val password: String)
