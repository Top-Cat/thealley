package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Tado")
data class TadoConfig(
    override val name: String,
    val email: String,
    val pass: String,
    val updateReadings: Boolean = false
) : IAlleyConfig