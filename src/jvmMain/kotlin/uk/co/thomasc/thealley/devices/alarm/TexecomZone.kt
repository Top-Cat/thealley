package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TexecomZone(
    val name: String,
    @Transient
    val slug: String? = null,
    val number: Int,
    val status: ZoneState,
    val type: TexecomZoneType,
    val areas: List<String>
)
