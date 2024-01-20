package uk.co.thomasc.thealley.devices.system.conditional

import kotlinx.serialization.Serializable

@Serializable
data class ConditionalState(
    val states: List<Boolean> = emptyList()
)
