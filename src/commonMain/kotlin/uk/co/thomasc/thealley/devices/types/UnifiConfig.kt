package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Unifi")
data class UnifiConfig(
    override val name: String,
    val mainNetwork: String,
    val guestNetwork: String,
    val guestPassword: String
) : IAlleyConfig
