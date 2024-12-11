package uk.co.thomasc.thealley.devices.state.kasa.bulb

import kotlinx.datetime.Instant
import uk.co.thomasc.thealley.devices.state.IAlleyState

interface ITriggerableState<T : ITriggerableState<T>> : IAlleyState {
    val daytime: Boolean
    val lastMotion: Instant?
    val offAt: Instant?
    val ignoreMotionUntil: Instant?

    fun toDaytime(): T
    fun toNighttime(offAt: Instant?): T
    fun clearOffAt(): T
    fun motion(now: Instant, offAt: Instant): T
}
