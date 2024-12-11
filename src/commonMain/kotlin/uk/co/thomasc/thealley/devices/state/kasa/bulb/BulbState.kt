package uk.co.thomasc.thealley.devices.state.kasa.bulb

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.kasa.IKasaState
import uk.co.thomasc.thealley.devices.state.zigbee.IZigbeeState

@Serializable
data class BulbState(
    override val daytime: Boolean = true,
    override val lastMotion: Instant? = null,
    override val offAt: Instant? = null,
    override val ignoreMotionUntil: Instant? = null
) : IKasaState, IZigbeeState, ITriggerableState<BulbState> {
    override fun toDaytime() = copy(daytime = true)
    override fun toNighttime(offAt: Instant?) = copy(daytime = false, offAt = offAt)
    override fun clearOffAt() = copy(offAt = null)
    override fun motion(now: Instant, offAt: Instant) = copy(lastMotion = now, offAt = offAt)
}
