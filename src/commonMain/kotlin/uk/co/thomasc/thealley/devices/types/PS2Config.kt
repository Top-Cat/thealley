package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PS2")
data class PS2Config(
    override val name: String,
    val prefix: String = "ps2"
) : IAlleyConfig
