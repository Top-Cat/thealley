package uk.co.thomasc.thealley.web.stats

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GenericResponse(
    val id: Int,
    val extra: Map<String, JsonElement>
)
