package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TexecomInfo(
    val version: String,
    @SerialName("log_level")
    val logLevel: String,
    val model: String,
    @SerialName("firmware_version")
    val firmwareVersion: String,
    @SerialName("serial_number")
    val serialNumber: String
)
