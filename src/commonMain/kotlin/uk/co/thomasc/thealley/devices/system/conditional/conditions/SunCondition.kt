package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.serialization.Serializable

@Serializable
data class SunCondition(
    val daytime: Boolean
) : ICondition