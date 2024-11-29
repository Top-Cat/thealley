package uk.co.thomasc.thealley.devices.alarm.events

import uk.co.thomasc.thealley.devices.alarm.TexecomAreaStatus

data class TexecomAreaEvent(
    val areaId: Int,
    val areaName: String,
    val status: TexecomAreaStatus
) : ITexecomEvent
