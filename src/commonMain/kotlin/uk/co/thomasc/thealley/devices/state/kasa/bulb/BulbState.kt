package uk.co.thomasc.thealley.devices.state.kasa.bulb

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState
import uk.co.thomasc.thealley.devices.state.kasa.IKasaState

@Serializable
data class BulbState(
    val daytime: Boolean = true,
    val lastMotion: Instant? = null,
    val offAt: Instant? = null,
    val ignoreMotionUntil: Instant? = null
) : IKasaState, IAlleyState
