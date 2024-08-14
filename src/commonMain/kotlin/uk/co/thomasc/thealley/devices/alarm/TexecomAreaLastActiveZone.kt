package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.Serializable

@Serializable
data class TexecomAreaLastActiveZone(
    val name: String,
    val number: Int
)
