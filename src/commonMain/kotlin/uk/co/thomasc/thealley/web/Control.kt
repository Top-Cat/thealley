package uk.co.thomasc.thealley.web

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.types.IAlleyConfig

@Serializable
data class DeviceInfo(val id: Int, val config: IAlleyConfig<*>)

@Serializable
data class ControlResult(val success: Boolean)

@Serializable
data class BulbState(val state: Boolean, val brightness: Int?, val hue: Int?, val temp: Int?) {
    constructor(on: Boolean) : this(on, null, null, null)
}
