package uk.co.thomasc.thealley.devices

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.client.RelayMqtt

class Relay(
    private val host: String,
    private val restTemplate: HttpClient,
    private val apiKey: String,
    private val mqtt: RelayMqtt.DeviceGateway,
    val props: MutableMap<String, Any> = mutableMapOf(),
    private var state: Boolean? = null
) : Light<Unit> {

    override fun setPowerState(value: Boolean) =
        setLightState(if (value) 1 else 0)

    override fun setComplexState(brightness: Int?, hue: Int?, saturation: Int?, temperature: Int?, transitionTime: Int?) {
        brightness?.let {
            setPowerState(brightness > 50)
        }
    }

    private fun setLightState(state: Int) {
        mqtt.sendToMqtt("$host/relay/0/set", MqttMessage("$state".toByteArray()))
    }

    override suspend fun getPowerState() = state ?: run {
        val newState = try {
            restTemplate.get("http://$host.light.kirkstall.top-cat.me/api/relay/0?apikey=$apiKey") {
                accept(ContentType.Any)
            }.body<Int>() > 0
        } catch (e: Exception) {
            false
        }

        // Temporary variable prevents null return type
        state = newState

        newState
    }

    override suspend fun togglePowerState() = setLightState(2)

    fun handleMessage(topic: String, message: MqttMessage) {
        Regex("([^/,]+)/([^/,]+)(?:/([^/,]+))?").find(topic)?.also {
            val (str, host, prop, idx) = it.groupValues

            when (prop) {
                "relay" -> {
                    state = message.toString() == "1"
                }
                "button" -> {
                    runBlocking {
                        togglePowerState()
                    }
                }
                else -> props[prop] = try {
                    message.toString().toDouble()
                } catch (e: NumberFormatException) {
                    message.toString()
                }
            }
        } ?: run {
            println("Couldn't parse MQTT message")
            println(topic)
            println(message.payload)
        }
    }
}
