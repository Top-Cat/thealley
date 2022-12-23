package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.Config
import uk.co.thomasc.thealley.devices.Blind
import uk.co.thomasc.thealley.devices.Relay
import uk.co.thomasc.thealley.rest.Api
import uk.co.thomasc.thealley.rest.PropertyData
import uk.co.thomasc.thealley.scenes.SceneController

data class RelayState(@JsonAlias("relay/0") val relay0: Boolean)
data class ZigbeeUpdate(
    // Generic
    val linkquality: Int,
    val battery: Int?,

    // Light sensor
    val voltage: Int?,

    val illuminance: Int,
    val illuminance_lux: Int?,
    val occupancy: Boolean?,

    // Blind motor
    val device_temperature: Int?,
    val motor_state: BlindMotorState?,
    val position: Int?,
    val power_outage_count: Int?,
    val running: Boolean?,
    val state: BlindState?
)
enum class BlindMotorState { DECLINING, RISING, PAUSE, BLOCKED }
enum class BlindState { ON, OFF }

class RelayMqtt(val client: MqttClient, val relayClient: RelayClient, val sceneController: SceneController, val api: Api) {
    init {
        client.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable) {
                println("connectionLost")
                cause.printStackTrace()
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val host = topic.substring(0, topic.indexOf("/"))

                if (host == "zigbee") {
                    val deviceId = topic.substring(host.length + 1)

                    // Check this is a message isn't from the bridge
                    if (!deviceId.startsWith("0x") || deviceId.contains('/')) return

                    val update = jackson.readValue<ZigbeeUpdate>(message.toString())
                    println("Received zigbee message, device: $deviceId, data: $update")

                    if (update.motor_state != null) {
                        relayClient.getBlind(deviceId).handleMessage(update)
                    } else {
                        if (update.occupancy == true) {
                            runBlocking {
                                sceneController.onChange(deviceId)
                            }
                        }
                        api.onPropertyChange(PropertyData(deviceId, "illuminance", update.illuminance.toDouble()))
                        update.battery?.let {
                            api.onPropertyChange(PropertyData(deviceId, "battery", it.toDouble()))
                        }
                        update.voltage?.let {
                            api.onPropertyChange(PropertyData(deviceId, "voltage", it.toDouble()))
                        }
                    }
                } else {
                    relayClient.getRelay(host).handleMessage(topic, message)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                println("deliveryComplete")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                println("connectComplete")
                client.subscribe("#")
            }
        })
    }

    interface DeviceGateway {
        fun sendToMqtt(topic: String, payload: MqttMessage)
    }
}

class RelayClient(val config: Config, val mqtt: RelayMqtt.DeviceGateway) {

    private val relayMap = mutableMapOf<String, Relay>()
    private val blindMap = mutableMapOf<String, Blind>()

    fun getRelay(host: String) = relayMap.getOrPut(host.uppercase()) {
        Relay(host, client, config.relay.apiKey, mqtt)
    }

    fun getBlind(deviceId: String) = blindMap.getOrPut(deviceId) {
        Blind(deviceId, mqtt)
    }
}
