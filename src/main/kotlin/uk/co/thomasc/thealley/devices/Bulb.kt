package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.experimental.runBlocking
import mu.KLogging

class Bulb(host: String) : KasaDevice<BulbData>(host), Light<Bulb> {

    companion object : KLogging()

    private var bulbData: BulbData? = null

    @Synchronized
    override fun getData() = (bulbData ?: updateData())!!

    @Synchronized
    private fun updateData() =
        runBlocking { getSysInfo(host, 5000) as? BulbData }?.apply {
            bulbData = this
        }

    fun getName() = getData().alias
    override fun getPowerState() = getData().light_state.on_off
    fun getLightState() = getData().light_state
    fun getSignalStrength() = getData().rssi
    fun getPowerUsage() = getPower().power_mw

    fun getHwVer() = getData().hw_ver
    fun getSwVer() = getData().sw_ver
    fun getModel() = getData().model

    @Synchronized
    private fun getPower() =
        runBlocking {
            // May as well get sysinfo while we're at it
            send("{\"system\":{\"get_sysinfo\":{}},\"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}")?.let {
                (parseSysInfo(it) as? BulbData)?.apply {
                    bulbData = this
                }

                mapper.readValue<BulbEmeterResponse>(it).emeter.get_realtime
            } ?: BulbRealtimePower(0, -1)
        }

    override fun setPowerState(value: Boolean): Bulb =
        runBlocking {
            setLightState(BulbUpdate(value))
        }

    override fun setComplexState(brightness: Int?, hue: Int?, saturation: Int?, temperature: Int?, transitionTime: Int?) =
        runBlocking {
            setLightState(temperature?.let {
                BulbUpdate(transitionTime, true, null, null, null, brightness, temperature)
            } ?: hue?.let {
                BulbUpdate(transitionTime, true, null, hue, saturation, brightness, 0)
            } ?: BulbUpdate(transitionTime, (brightness ?: 1) > 0, null, null, null, brightness, null))
        }

    private suspend fun setLightState(state: BulbUpdate): Bulb {
        val obj = LightingServiceUpdate(LightingService(state))

        send(mapper.writeValueAsString(obj))?.let {
            val result = mapper.readValue<LightingServiceUpdate>(it)
            bulbData = getData()?.copy(light_state = result.lightingService.transition_light_state)
        }

        return this
    }

    override fun togglePowerState() =
        setPowerState(!getPowerState())

    private suspend fun send(json: String) = send(json, host, timeout = 500)
}
