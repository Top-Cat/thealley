package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import uk.co.thomasc.thealley.client.jackson

class Plug(host: String) : KasaDevice<PlugData>(host) {

    private var plugData: PlugData? = null

    @Synchronized
    override suspend fun getData() = (plugData ?: updateData())!!

    @Synchronized
    suspend fun updateData() =
        (getSysInfo(host, 5000) as? PlugData)?.apply {
            plugData = this
        }

    suspend fun getName() = getData().alias
    suspend fun getPowerState() = getData().relay_state
    suspend fun getUptime() = getData().on_time
    suspend fun getSignalStrength() = getData().rssi

    @Synchronized
    fun getPower() =
        runBlocking {
            // May as well get sysinfo while we're at it
            send("{\"system\":{\"get_sysinfo\":{}},\"emeter\":{\"get_realtime\":{}}}")?.let {
                (parseSysInfo(it) as? PlugData)?.apply {
                    plugData = this
                }

                jackson.readValue<EmeterResponse>(it).emeter.get_realtime
            } ?: RealtimePower(0F, 0F, 0F, 0F, -1)
        }

    /*fun setPowerState(value: Boolean): Plug =
        runBlocking {
            setLightState(BulbUpdate(value))
        }

    private suspend fun setPlugState(state: BulbUpdate): Plug {
        val obj = LightingServiceUpdate(LightingService(state))
        val response = send(mapper.writeValueAsString(obj))

        val result = mapper.readValue<LightingServiceUpdate>(response)

        return Plug(client, host, bulb.copy(light_state = result.lightingService.transition_light_state))
    }*/

    private suspend fun send(json: String) = send(json, host, timeout = 500)
}
