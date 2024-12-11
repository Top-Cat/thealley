package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.state.kasa.bulb.ITriggerableState
import kotlin.time.Duration

interface ITriggerableConfig<T : ITriggerableState<T>> : IAlleyConfig<T> {
    val timeout: Duration
    val sensors: List<Int>
}
