package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Mqtt")
data class MqttConfig(override val name: String, val clientId: String, val host: String, val user: String, val pass: String) : IAlleyConfig
