package uk.co.thomasc.thealley.web

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.types.IAlleyConfig

@Serializable
data class DeviceInfo(val id: Int, val config: IAlleyConfig)

@Serializable
data class ControlResult(val success: Boolean)

@Serializable
data class BulbState(val state: Int, val dimmable: Boolean, val hue: Int?, val temp: Int?, val color: Boolean) {
    constructor(on: Boolean) : this(if (on) 100 else 0, false, 0, 0, false)
    constructor(state: Int, hue: Int?, temp: Int?) : this(state, true, hue, temp, true)
}
