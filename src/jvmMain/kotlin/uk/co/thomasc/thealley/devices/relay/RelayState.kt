package uk.co.thomasc.thealley.devices.relay

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RelayState(
    val on: Boolean,
    val daytime: Boolean = true,
    val lastMotion: Instant? = null,
    val offAt: Instant? = null,
    val ignoreMotionUntil: Instant? = null
)
