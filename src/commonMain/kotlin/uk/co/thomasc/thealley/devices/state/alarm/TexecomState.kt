package uk.co.thomasc.thealley.devices.state.alarm

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.alarm.GoogleArmLevel
import uk.co.thomasc.thealley.devices.alarm.TexecomArea
import uk.co.thomasc.thealley.devices.alarm.TexecomZone
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class TexecomState(
    val armLevel: GoogleArmLevel = GoogleArmLevel.NONE,
    val areaState: Map<String, TexecomArea> = mapOf(),
    val zoneState: Map<Int, TexecomZone> = mapOf()
) : IAlleyState
