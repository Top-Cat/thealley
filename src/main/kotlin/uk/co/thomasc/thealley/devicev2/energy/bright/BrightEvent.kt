package uk.co.thomasc.thealley.devicev2.energy.bright

import kotlinx.datetime.Instant
import uk.co.thomasc.thealley.devicev2.IAlleyEvent

data class BrightEvent(val meterTotal: Double, val latestReading: Instant) : IAlleyEvent
