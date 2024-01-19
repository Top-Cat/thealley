package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Texecom")
data class TexecomConfig(
    override val name: String,
    val prefix: String = "texecom2mqtt"
) : IAlleyConfig
