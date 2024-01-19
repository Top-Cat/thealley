package uk.co.thomasc.thealley.devices.alarm

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class TexecomEvent(
    val type: TexecomEventType,
    val description: String,
    val timestamp: Instant,
    val areas: List<String>,
    val parameter: Int,
    val entity: JsonElement? = null,
    val groupType: EventGroupType
)
