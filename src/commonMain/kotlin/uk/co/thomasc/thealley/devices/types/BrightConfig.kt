package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Bright")
data class BrightConfig(override val name: String, val email: String, val pass: String) : IAlleyConfig