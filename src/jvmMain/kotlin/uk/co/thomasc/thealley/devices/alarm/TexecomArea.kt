package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TexecomArea(
    val id: String,
    val name: String,
    @Transient
    val slug: String? = null,
    val number: Int,
    val status: TexecomAreaStatus,
    @SerialName("last_active_zone")
    val lastActiveZone: TexecomAreaLastActiveZone? = null
)
