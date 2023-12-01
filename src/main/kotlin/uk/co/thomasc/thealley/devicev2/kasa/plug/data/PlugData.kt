package uk.co.thomasc.thealley.devicev2.kasa.plug.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface KasaResponse<T : KasaData> {
    val system: KasaSysInfo<T>
}

interface KasaEmeter<T : KasaErrCode> {
    val realtime: T
}

interface KasaErrCode {
    val errorCode: Int
}

@Serializable
data class PlugResponse(
    override val system: KasaSysInfo<PlugData>,
    val emeter: Emeter? = null
) : KasaResponse<PlugData>

@Serializable
data class KasaSysInfo<T : KasaData>(
    @SerialName("get_sysinfo")
    val sysInfo: T
)

interface KasaData : KasaErrCode {
    val swVer: String
    val hwVer: String
    val model: String
    val type: String
    val alias: String
    val mac: String
    val deviceId: String
    val hwId: String
    val oemId: String
    val activeMode: String
    val latitude: Float
    val longitude: Float
    val rssi: Int
}

@Serializable
data class PlugData(
    @SerialName("sw_ver")
    override val swVer: String,
    @SerialName("hw_ver")
    override val hwVer: String,
    override val type: String,
    override val model: String,
    override val mac: String,
    override val deviceId: String,
    override val hwId: String,
    val fwId: String,
    override val oemId: String,
    override val alias: String,
    @SerialName("dev_name")
    val devName: String,
    @SerialName("icon_hash")
    val iconHash: String,
    @SerialName("relay_state")
    val relayState: Int,
    @SerialName("on_time")
    val onTime: Int,
    @SerialName("active_mode")
    override val activeMode: String,
    val feature: String,
    val updating: Int,
    override val rssi: Int,
    @SerialName("led_off")
    val ledOff: Int,
    override val latitude: Float,
    override val longitude: Float,
    @SerialName("err_code")
    override val errorCode: Int
) : KasaData {
    fun getRelayState() = relayState > 0
}

@Serializable
data class Emeter(
    @SerialName("get_realtime")
    override val realtime: RealtimePower
) : KasaEmeter<RealtimePower>

@Serializable
data class RealtimePower(
    val current: Float,
    val voltage: Float,
    val power: Float,
    val total: Float,
    @SerialName("err_code")
    override val errorCode: Int
) : KasaErrCode
