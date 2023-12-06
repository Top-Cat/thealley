package uk.co.thomasc.thealley.web.stats

import kotlinx.serialization.Serializable

@Serializable
data class TadoResponse(
    val homeId: Int,
    val zoneStates: List<TransformedZoneState>
)
