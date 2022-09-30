package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import mu.KLogging
import uk.co.thomasc.thealley.client.jackson
import java.time.Instant

class Bulb(host: String) : KasaDevice<BulbData>(host), Light<Bulb> {

    companion object : KLogging()

    private var bulbData: BulbData? = null

    @Synchronized
    override suspend fun getData() = (bulbData ?: updateData())

    private var lastRequest: Instant = Instant.MIN
    @Synchronized
    private suspend fun updateData() =
        run {
            if (Instant.now().minusSeconds(20).isAfter(lastRequest)) {
                (getSysInfo(host, 3000) as? BulbData)?.apply {
                    bulbData = this
                }
            } else null
        }.also {
            lastRequest = Instant.now()
        }

    suspend fun getName() = getData()?.alias
    override suspend fun getPowerState() = getData()?.light_state?.on_off == true
    suspend fun getLightState() = getData()?.light_state
    suspend fun getSignalStrength() = getData()?.rssi
    suspend fun getPowerUsage() = getPower().power_mw

    suspend fun getHwVer() = getData()?.hw_ver
    suspend fun getSwVer() = getData()?.sw_ver
    suspend fun getModel() = getData()?.model

    @Synchronized
    private fun getPower() =
        runBlocking {
            // May as well get sysinfo while we're at it
            send("{\"system\":{\"get_sysinfo\":{}},\"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}")?.let {
                (parseSysInfo(it) as? BulbData)?.apply {
                    bulbData = this
                }

                jackson.readValue<BulbEmeterResponse>(it).emeter.get_realtime
            } ?: BulbRealtimePower(0, -1)
        }

    override fun setPowerState(value: Boolean): Bulb =
        runBlocking {
            setLightState(BulbUpdate(value))
        }

    override fun setComplexState(brightness: Int?, hue: Int?, saturation: Int?, temperature: Int?, transitionTime: Int?) =
        runBlocking {
            setLightState(
                temperature?.let {
                    BulbUpdate(transitionTime, true, null, null, null, brightness, temperature)
                } ?: hue?.let {
                    BulbUpdate(transitionTime, true, null, hue, saturation, brightness, 0)
                } ?: BulbUpdate(transitionTime, (brightness ?: 1) > 0, null, null, null, brightness, null)
            )
        }

    private suspend fun setLightState(state: BulbUpdate): Bulb {
        val obj = LightingServiceUpdate(LightingService(state))

        send(jackson.writeValueAsString(obj))?.let {
            val result = jackson.readValue<LightingServiceUpdate>(it)
            bulbData = getData()?.copy(light_state = result.lightingService.transition_light_state)
        }

        return this
    }

    override suspend fun togglePowerState() =
        setPowerState(!getPowerState())

    private suspend fun send(json: String) = send(json, host, timeout = 500)
}
