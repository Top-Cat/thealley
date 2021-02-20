package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class LightingServiceUpdate(
    @JsonProperty("smartlife.iot.smartbulb.lightingservice")
    var lightingService: LightingService
)

data class LightingService(val transition_light_state: BulbUpdate)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BulbUpdate(
    override val transition_period: Int?,
    override val on_off: Boolean,
    override val mode: String?,
    override val hue: Int?,
    override val saturation: Int?,
    override val brightness: Int?,
    override val color_temp: Int?,
    override val ignore_default: Boolean = true
) : IBulbUpdate() {
    constructor(on_off: Boolean, transition_period: Int? = null) : this(transition_period, on_off, null, null, null, null, null)
}

abstract class IBulbUpdate : IBulbState() {
    abstract val transition_period: Int?
    abstract val ignore_default: Boolean

    @JsonGetter("ignore_default")
    private fun getIgnoreDefault() = if (ignore_default) 1 else 0
}

abstract class IBulbState : IBulbOnOff() {
    abstract val mode: String?
    abstract val hue: Int?
    abstract val saturation: Int?
    abstract val brightness: Int?
    abstract val color_temp: Int?
}

@JsonIgnoreProperties(ignoreUnknown = true)
abstract class IBulbOnOff {
    abstract val on_off: Boolean

    @JsonGetter("on_off")
    private fun getState() = if (on_off) 1 else 0
}

/*data class BulbState(
    override val on_off: Boolean,
    override val mode: String?,
    override val hue: Int?,
    override val saturation: Int?,
    override val brightness: Int?,
    override val color_temp: Int?
) : IBulbState()

@JsonIgnoreProperties(ignoreUnknown = true)
data class BulbOnOff(override val on_off: Boolean) : IBulbOnOff()*/

@JsonIgnoreProperties(ignoreUnknown = true)
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
    val light_state: BulbUpdate,
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class BulbEmeterResponse(
    @JsonProperty("smartlife.iot.common.emeter")
    val emeter: BulbEmeter
)

data class BulbEmeter(val get_realtime: BulbRealtimePower)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BulbRealtimePower(
    val power_mw: Int,
    val err_code: Int
)
