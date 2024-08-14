package uk.co.thomasc.thealley.devices.state.energy.bright

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class BrightState(val meterTotal: Double, val nextCatchup: Instant? = null, val latestReading: Instant) : IAlleyState
