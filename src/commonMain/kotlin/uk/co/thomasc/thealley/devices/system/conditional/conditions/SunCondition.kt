package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Sun")
data class SunCondition(
    val daytime: Boolean
) : ICondition
