package uk.co.thomasc.thealley.web.stats

import kotlinx.serialization.Serializable

@Serializable
data class BulbResponse(
    val host: String,
    val name: String? = null,
    val state: Int,
    val power: Int,
    val rssi: Int? = null
)
