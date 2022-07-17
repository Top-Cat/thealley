package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.Config
import uk.co.thomasc.thealley.devices.Relay
import uk.co.thomasc.thealley.rest.Api
import uk.co.thomasc.thealley.rest.PropertyData
import uk.co.thomasc.thealley.scenes.SceneController

data class RelayState(@JsonAlias("relay/0") val relay0: Boolean)
data class ZigbeeUpdate(val illuminance: Int, val linkquality: Int, val occupancy: Boolean?, val battery: Int?, val voltage: Int?, val illuminance_lux: Int?)

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
                    if (!deviceId.startsWith("0x")) return

                    val update = jackson.readValue<ZigbeeUpdate>(message.toString())
                    println("Received zigbee message, device: $deviceId, data: $update")

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
                } else {
                    relayClient.getRelay(host).let {
                        runBlocking {
                            it.handleMessage(topic, message)
                        }
                    }
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

    fun getRelay(host: String) = relayMap.getOrPut(host.uppercase()) {
        Relay(host, client, config.relay.apiKey, mqtt)
    }
}
