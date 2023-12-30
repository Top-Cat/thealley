package uk.co.thomasc.thealley.web.stats

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RelayResponse(
    val host: String,
    val state: Int,
    val extra: Map<String, JsonElement>
)
