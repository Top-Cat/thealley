package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.alarm.ZoneState

@Serializable
data class TexecomZoneCondition(
    val zoneId: Int,
    val zoneState: ZoneState
) : ICondition