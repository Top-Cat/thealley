package uk.co.thomasc.thealley.devices.alarm.events

import uk.co.thomasc.thealley.devices.alarm.ZoneState

data class TexecomZoneEvent(
    val zoneId: Int,
    val status: ZoneState
) : ITexecomEvent
