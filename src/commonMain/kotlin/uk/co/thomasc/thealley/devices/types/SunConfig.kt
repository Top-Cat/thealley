package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Sun")
data class SunConfig(override val name: String, val lat: Double, val lon: Double, val tz: String) : IAlleyConfig
