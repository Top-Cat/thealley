package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.experimental.runBlocking
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.mapper

class Bulb(private val client: LocalClient, private val host: String, private val bulb: BulbData) : Light<Bulb> {

    fun getName() = bulb.alias
    fun getPowerState() = bulb.light_state.on_off
    fun getSignalStrength() = bulb.rssi
    fun getPowerUsage() = realtimePower.power_mw

    private val realtimePower by lazy {
        runBlocking {
            send("{\"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}")?.let {
                mapper.readValue<BulbEmeterResponse>(it).emeter.get_realtime
            } ?: BulbRealtimePower(0, -1)
        }
    }

    override fun setPowerState(value: Boolean): Bulb =
        runBlocking {
            setLightState(BulbUpdate(value))
        }

    override fun setComplexState(brightness: Int, hue: Int) =
        runBlocking {
            setLightState(BulbUpdate(1000, true, null, hue, 0, brightness, null))
        }

    private suspend fun setLightState(state: BulbUpdate): Bulb {
        val obj = LightingServiceUpdate(LightingService(state))

        val newBulb = send(mapper.writeValueAsString(obj))?.let {
            val result = mapper.readValue<LightingServiceUpdate>(it)
            bulb.copy(light_state = result.lightingService.transition_light_state)
        } ?: bulb

        return Bulb(client, host, newBulb)
    }

    fun togglePowerState() =
        setPowerState(!getPowerState())

    private suspend fun send(json: String) = client.send(json, host)
}

interface Light<out T> {

    fun setPowerState(value: Boolean): T

    fun setComplexState(brightness: Int, hue: Int): T

}
