package uk.co.thomasc.thealley.devices

import kotlinx.serialization.Serializable

@Serializable
data class PlugData(
    val sw_ver: String,
    val hw_ver: String,
    val type: String,
    val model: String,
    val mac: String,
    val deviceId: String,
    val hwId: String,
    val fwId: String,
    val oemId: String,
    val alias: String,
    val dev_name: String,
    val icon_hash: String,
    val relay_state: Int,
    val on_time: Int,
    val active_mode: String,
    val feature: String,
    val updating: Int,
    val rssi: Int,
    val led_off: Int,
    val latitude: Float,
    val longitude: Float,
    val err_code: Int
) {
    fun getRelayState() = relay_state > 0
}

@Serializable
data class EmeterResponse(val emeter: Emeter)
@Serializable
data class Emeter(val get_realtime: RealtimePower)
@Serializable
data class RealtimePower(
    val current: Float,
    val voltage: Float,
    val power: Float,
    val total: Float,
    val err_code: Int
)
