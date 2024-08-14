package uk.co.thomasc.thealley.devices.state.relay

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class RelayState(
    val on: Boolean,
    val daytime: Boolean = true,
    val lastMotion: Instant? = null,
    val offAt: Instant? = null,
    val ignoreMotionUntil: Instant? = null
) : IAlleyState
