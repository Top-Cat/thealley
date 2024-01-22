package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.Serializable

@Serializable
data class TexecomZone(
    val name: String,
    val slug: String? = null,
    val number: Int,
    val status: ZoneState,
    val type: TexecomZoneType,
    val areas: List<String>
)
