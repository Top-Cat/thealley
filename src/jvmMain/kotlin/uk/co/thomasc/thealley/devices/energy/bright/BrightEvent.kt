package uk.co.thomasc.thealley.devices.energy.bright

import kotlinx.datetime.Instant
import uk.co.thomasc.thealley.devices.system.IAlleyEvent

data class BrightEvent(val meterTotal: Double, val latestReading: Instant) : IAlleyEvent
