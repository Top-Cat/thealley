package uk.co.thomasc.thealley.devices.alarm.events

import uk.co.thomasc.thealley.devices.alarm.TexecomAreaStatus

data class TexecomAreaEvent(
    val areaId: Int,
    val status: TexecomAreaStatus
) : ITexecomEvent
