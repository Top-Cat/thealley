package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.experimental.runBlocking
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.mapper

class Plug(private val client: LocalClient, private val host: String, private val plug: PlugData) {

    fun getName() = plug.alias
    fun getPowerState() = plug.relay_state
    fun getVoltage() = realtimePower.voltage
    fun getCurrent() = realtimePower.current
    fun getPowerUsage() = realtimePower.power
    fun getUptime() = plug.on_time
    fun getSignalStrength() = plug.rssi

    val realtimePower by lazy {
        runBlocking {
            val response = send("{\"emeter\":{\"get_realtime\":{}}}")

            val result = mapper.readValue<EmeterResponse>(response)

            result.emeter.get_realtime
        }
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

    private suspend fun send(json: String) = client.send(json, host)
}
