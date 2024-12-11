package uk.co.thomasc.thealley.devices.state.relay

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState
import uk.co.thomasc.thealley.devices.state.kasa.bulb.ITriggerableState

@Serializable
data class RelayState(
    val on: Boolean,
    override val daytime: Boolean = true,
    override val lastMotion: Instant? = null,
    override val offAt: Instant? = null,
    override val ignoreMotionUntil: Instant? = null
) : IAlleyState, ITriggerableState<RelayState> {
    override fun toDaytime() = copy(daytime = true)
    override fun toNighttime(offAt: Instant?) = copy(daytime = false, offAt = offAt)
    override fun clearOffAt() = copy(offAt = null)
    override fun motion(now: Instant, offAt: Instant) = copy(lastMotion = now, offAt = offAt)
}
