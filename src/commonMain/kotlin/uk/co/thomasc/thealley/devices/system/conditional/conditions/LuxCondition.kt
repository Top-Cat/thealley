package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Lux")
data class LuxCondition(
    val deviceId: Int,
    val lux: Int = -1,
    val above: Boolean = true
) : ICondition
