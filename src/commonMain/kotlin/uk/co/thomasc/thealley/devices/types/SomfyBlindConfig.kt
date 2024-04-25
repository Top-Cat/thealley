package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SomfyBlind")
data class SomfyBlindConfig(
    override val name: String,
    val deviceId: String,
    val prefix: String = "espsomfy"
) : IAlleyConfig
