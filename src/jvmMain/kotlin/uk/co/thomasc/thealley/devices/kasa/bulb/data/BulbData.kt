package uk.co.thomasc.thealley.devices.kasa.bulb.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.generic.ILightState
import uk.co.thomasc.thealley.devices.kasa.plug.data.KasaData
import uk.co.thomasc.thealley.devices.kasa.plug.data.KasaEmeter
import uk.co.thomasc.thealley.devices.kasa.plug.data.KasaErrCode
import uk.co.thomasc.thealley.devices.kasa.plug.data.KasaResponse
import uk.co.thomasc.thealley.devices.kasa.plug.data.KasaSysInfo

@Serializable
data class BulbResponse(
    override val system: KasaSysInfo<BulbData>,
    @SerialName("smartlife.iot.common.emeter")
    val emeter: BulbEmeter? = null
) : KasaResponse<BulbData>

@Serializable
data class LightingServiceUpdate(
    @SerialName("smartlife.iot.smartbulb.lightingservice")
    val lightingService: LightingService
)

@Serializable
data class LightingService(
    @SerialName("transition_light_state")
    val transitionLightState: BulbUpdate
)

@Serializable
class BulbUpdate private constructor(
    @SerialName("transition_period")
    override val transitionPeriod: Int? = null,
    @SerialName("on_off")
    override val onOff: Int,
    override val mode: String? = null,
    override val hue: Int? = null,
    override val saturation: Int? = null,
    override val brightness: Int? = null,
    @SerialName("color_temp")
    override val temperature: Int? = null,
    @SerialName("ignore_default")
    override val ignoreDefault: Int? = null,
    @SerialName("dft_on_state")
    override val dftOnState: DftOnState? = null,
    @SerialName("err_code")
    override val errorCode: Int = 0
) : IBulbUpdate, KasaErrCode {
    constructor(
        transitionPeriod: Int? = null,
        onOff: Boolean,
        mode: String? = null,
        hue: Int? = null,
        saturation: Int? = null,
        brightness: Int? = null,
        temperature: Int? = null,
        ignoreDefault: Boolean = true
    ) : this(transitionPeriod, if (onOff) 1 else 0, mode, hue, saturation, brightness, temperature, if (ignoreDefault) 1 else 0)
    constructor(onOff: Boolean, transitionPeriod: Int? = null) : this(transitionPeriod, if (onOff) 1 else 0, null, null, null, null, null, 1)

    fun isOn() = onOff > 0
}

@Serializable
data class DftOnState(
    override val mode: String?,
    override val hue: Int?,
    override val saturation: Int?,
    override val brightness: Int?,
    @SerialName("color_temp")
    override val temperature: Int?
) : IBulbState

interface IBulbUpdate : IBulbState, IBulbOnOff {
    val transitionPeriod: Int?
    val ignoreDefault: Int?
    val dftOnState: DftOnState?
}

interface IBulbState : ILightState {
    val mode: String?
}

interface IBulbOnOff {
    val onOff: Int
}

@Serializable
data class BulbData(
    @SerialName("sw_ver")
    override val swVer: String,
    @SerialName("hw_ver")
    override val hwVer: String,
    override val model: String,
    val description: String,
    override val alias: String,
    @SerialName("mic_type")
    override val type: String,
    @SerialName("dev_state")
    val devState: String,
    @SerialName("mic_mac")
    override val mac: String,
    override val deviceId: String,
    override val oemId: String,
    override val hwId: String,
    @SerialName("is_factory")
    val isFactory: Boolean,
    @SerialName("disco_ver")
    val discoVer: String,
    @SerialName("ctrl_protocols")
    val ctrlProtocols: CtrlProtocol,
    @SerialName("light_state")
    val lightState: BulbUpdate,
    @SerialName("is_dimmable")
    val isDimmable: Int,
    @SerialName("is_color")
    val isColor: Int,
    @SerialName("is_variable_color_temp")
    val isVariableColorTemp: Int,
    @SerialName("preferred_state")
    val preferredState: List<PreferredState>,
    override val rssi: Int,
    @SerialName("active_mode")
    override val activeMode: String,
    @SerialName("heapsize")
    val heapSize: Int = 0,
    @SerialName("err_code")
    override val errorCode: Int,
    @SerialName("latitude_i")
    override val latitude: Float = 0f,
    @SerialName("longitude_i")
    override val longitude: Float = 0f,
    @SerialName("status")
    val status: String? = null
) : KasaData

@Serializable
data class PreferredState(
    val index: Int,
    override val hue: Int,
    override val saturation: Int,
    @SerialName("color_temp")
    override val temperature: Int,
    override val brightness: Int
) : ILightState

@Serializable
data class CtrlProtocol(
    val name: String,
    val version: String
)

@Serializable
data class BulbEmeterResponse(
    @SerialName("smartlife.iot.common.emeter")
    val emeter: BulbEmeter
)

@Serializable
data class BulbEmeter(
    @SerialName("get_realtime")
    override val realtime: BulbRealtimePower
) : KasaEmeter<BulbRealtimePower>

@Serializable
data class BulbRealtimePower(
    @SerialName("power_mw")
    val power: Int,
    @SerialName("total_wh")
    val total: Int? = 0,
    @SerialName("err_code")
    override val errorCode: Int
) : KasaErrCode
