package uk.co.thomasc.thealley.devices

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LightingServiceUpdate(
    @SerialName("smartlife.iot.smartbulb.lightingservice")
    val lightingService: LightingService
)

@Serializable
data class LightingService(val transition_light_state: BulbUpdate)

@Serializable
class BulbUpdate private constructor(
    override val transition_period: Int? = null,
    override val on_off: Int,
    override val mode: String? = null,
    override val hue: Int? = null,
    override val saturation: Int? = null,
    override val brightness: Int? = null,
    override val color_temp: Int? = null,
    override val ignore_default: Int? = null
) : IBulbUpdate() {
    constructor(transition_period: Int? = null, on_off: Boolean, mode: String? = null, hue: Int? = null, saturation: Int? = null, brightness: Int? = null, color_temp: Int? = null, ignore_default: Boolean = true) :
            this(transition_period, if (on_off) 1 else 0, mode, hue, saturation, brightness, color_temp, if (ignore_default) 1 else 0)
    constructor(on_off: Boolean, transition_period: Int? = null) : this(transition_period, if (on_off) 1 else 0, null, null, null, null, null, 1)

    fun isOn() = on_off > 0
}

abstract class IBulbUpdate : IBulbState() {
    abstract val transition_period: Int?
    abstract val ignore_default: Int?
}

abstract class IBulbState : IBulbOnOff() {
    abstract val mode: String?
    abstract val hue: Int?
    abstract val saturation: Int?
    abstract val brightness: Int?
    abstract val color_temp: Int?
}

abstract class IBulbOnOff {
    abstract val on_off: Int
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

@Serializable
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
    val is_dimmable: Int,
    val is_color: Int,
    val is_variable_color_temp: Int,
    val preferred_state: List<PreferredState>,
    val rssi: Int,
    val active_mode: String,
    val heapsize: Int = 0,
    val err_code: Int
)

@Serializable
data class PreferredState(
    val index: Int,
    val hue: Int,
    val saturation: Int,
    val color_temp: Int,
    val brightness: Int
)

@Serializable
data class CtrlProtocol(
    val name: String,
    val version: String
)

@Serializable
data class BulbEmeterResponse(val emeter: BulbEmeter)
@Serializable
data class BulbEmeter(val get_realtime: BulbRealtimePower)
@Serializable
data class BulbRealtimePower(val power_mw: Int, val err_code: Int)
