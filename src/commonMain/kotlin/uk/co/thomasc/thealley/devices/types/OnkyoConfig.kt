package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Onkyo")
data class OnkyoConfig(
    override val name: String,
    val host: String
) : IAlleyConfig
