package uk.co.thomasc.thealley.web.stats

import kotlinx.serialization.Serializable

@Serializable
data class PlugResponse(
    val host: String,
    val name: String,
    val state: Int,
    val power: Float,
    val voltage: Float,
    val current: Float,
    val uptime: Int,
    val rssi: Int
)
