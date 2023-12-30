package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SwitchServer")
data class SwitchServerConfig(override val name: String, val port: Int) : IAlleyConfig
