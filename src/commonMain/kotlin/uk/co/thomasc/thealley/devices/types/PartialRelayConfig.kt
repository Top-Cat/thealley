package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PartialRelay")
data class PartialRelayConfig(
    override val name: String,
    val device: Int,
    val index: Int
) : IAlleyConfig
