package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class LightingServiceUpdate(
    @JsonProperty("smartlife.iot.smartbulb.lightingservice")
    var lightingService: LightingService
)

data class LightingService(val transition_light_state: BulbOnOff)

@JsonInclude(JsonInclude.Include.NON_NULL)
class BulbUpdate(
    val transition_period: Int?,
    on_off: Boolean,
    mode: String?,
    hue: Int?,
    saturation: Int?,
    brightness: Int?,
    color_temp: Int?
): BulbState(on_off, mode, hue, saturation, brightness, color_temp) {
    constructor(on_off: Boolean) : this(null, on_off, null, null, null, null, null)
}

open class BulbState(
    on_off: Boolean,
    val mode: String?,
    val hue: Int?,
    val saturation: Int?,
    val brightness: Int?,
    val color_temp: Int?
): BulbOnOff(on_off) {
    constructor(on_off: Boolean) : this(on_off, null, null, null, null, null)
}

@JsonIgnoreProperties(ignoreUnknown = true)
open class BulbOnOff(val on_off: Boolean) {
    @JsonGetter("on_off")
    private fun getState() = if (on_off) 1 else 0
}

data class BulbData(
    val sw_ver: String,
    val hw_ver: String,
    val model: String,
    val description: String,
    val alias: String,
    val mic_type: String,
    val dev_state: String,
    val mic_mac: String,
    val deviceId: String,
    val oemId: String,
    val hwId: String,
    val is_factory: Boolean,
    val disco_ver: String,
    val ctrl_protocols: CtrlProtocol,
    val light_state: BulbOnOff,
    val is_dimmable: Boolean,
    val is_color: Boolean,
    val is_variable_color_temp: Boolean,
    val preferred_state: List<PreferredState>,
    val rssi: Int,
    val active_mode: String,
    val heapsize: Int,
    val err_code: Int
)

data class PreferredState(
    val index: Int,
    val hue: Int,
    val saturation: Int,
    val color_temp: Int,
    val brightness: Int
)

data class CtrlProtocol(
    val name: String,
    val version: String
)

data class BulbEmeterResponse(
    @JsonProperty("smartlife.iot.common.emeter")
    val emeter: BulbEmeter
)

data class BulbEmeter(val get_realtime: BulbRealtimePower)

data class BulbRealtimePower(
    val power_mw: Int,
    val err_code: Int
)
