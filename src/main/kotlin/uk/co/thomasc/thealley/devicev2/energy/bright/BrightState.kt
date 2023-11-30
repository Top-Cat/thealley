package uk.co.thomasc.thealley.devicev2.energy.bright

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class BrightState(val meterTotal: Double, val nextCatchup: Instant? = null, val latestReading: Instant)
