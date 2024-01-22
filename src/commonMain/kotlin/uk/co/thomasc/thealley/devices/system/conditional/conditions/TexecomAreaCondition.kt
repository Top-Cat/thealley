package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.alarm.TexecomAreaStatus

@Serializable
@SerialName("TexecomArea")
data class TexecomAreaCondition(
    val areaId: Int,
    val areaState: TexecomAreaStatus
) : ICondition
