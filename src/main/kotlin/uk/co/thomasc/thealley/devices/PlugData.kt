package uk.co.thomasc.thealley.devices

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
    val relay_state: Boolean,
    val on_time: Int,
    val active_mode: String,
    val feature: String,
    val updating: Boolean,
    val rssi: Int,
    val led_off: Boolean,
    val latitude: Float,
    val longitude: Float,
    val err_code: Int
)

data class EmeterResponse(val emeter: Emeter)

data class Emeter(val get_realtime: RealtimePower)

data class RealtimePower(
    val current: Float,
    val voltage: Float,
    val power: Float,
    val total: Float,
    val err_code: Int
)
