package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Relay")
data class RelayCondition(
    val state: Boolean
) : ICondition
