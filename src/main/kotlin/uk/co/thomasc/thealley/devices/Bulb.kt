package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.experimental.runBlocking
import uk.co.thomasc.thealley.LocalClient
import uk.co.thomasc.thealley.mapper

class Bulb(private val client: LocalClient, private val host: String, private val bulb: BulbData) {

    fun getName() = bulb.alias
    fun getPowerState() = bulb.light_state.on_off
    fun getSignalStrength() = bulb.rssi
    fun getPowerUsage() = realtimePower.power_mw

    private val realtimePower by lazy {
        runBlocking {
            val response = send("{\"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}")

            val result = mapper.readValue<BulbEmeterResponse>(response)

            result.emeter.get_realtime
        }
    }

    fun setPowerState(value: Boolean): Bulb =
        runBlocking {
            setLightState(BulbUpdate(value))
        }

    private suspend fun setLightState(state: BulbUpdate): Bulb {
        val obj = LightingServiceUpdate(LightingService(state))
        val response = send(mapper.writeValueAsString(obj))

        val result = mapper.readValue<LightingServiceUpdate>(response)

        return Bulb(client, host, bulb.copy(light_state = result.lightingService.transition_light_state))
    }

    fun togglePowerState() =
        setPowerState(!getPowerState())

    private suspend fun send(json: String) = client.send(json, host)
}
