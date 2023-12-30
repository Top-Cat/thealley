package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Plug")
data class PlugConfig(override val name: String, override val host: String) : IAlleyConfig, IKasaConfig
