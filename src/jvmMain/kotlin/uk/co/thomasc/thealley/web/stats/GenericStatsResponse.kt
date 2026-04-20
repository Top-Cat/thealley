package uk.co.thomasc.thealley.web.stats

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GenericStatsResponse(
    val id: Int,
    val name: String,
    val extra: Map<String, JsonElement>
)
