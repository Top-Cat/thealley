package uk.co.thomasc.thealley.devices.state.system.conditional

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class ConditionalState(
    val states: List<Boolean> = emptyList()
) : IAlleyState
