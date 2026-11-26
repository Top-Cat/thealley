package uk.co.thomasc.thealley.devices.system.conditional.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Onkyo")
data class OnkyoConditionAction(
    val deviceId: Int,
    val input: String? = null,
    val volume: Int? = null
) : IConditionAction
