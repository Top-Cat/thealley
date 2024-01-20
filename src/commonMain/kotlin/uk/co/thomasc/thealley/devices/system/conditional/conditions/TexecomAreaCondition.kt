package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.alarm.TexecomAreaStatus

@Serializable
data class TexecomAreaCondition(
    val areaId: Int,
    val areaState: TexecomAreaStatus
) : ICondition
