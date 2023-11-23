package uk.co.thomasc.thealley.client

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import mu.KLogging
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.Config
import uk.co.thomasc.thealley.devices.Blind
import uk.co.thomasc.thealley.devices.Relay
import uk.co.thomasc.thealley.devices.ZPlug
import uk.co.thomasc.thealley.rest.Api
import uk.co.thomasc.thealley.rest.PropertyData
import uk.co.thomasc.thealley.scenes.SceneController

val debugMqtt = System.getenv("MQTT_DEBUG") == "true"

interface ZigbeeUpdate {
    val linkquality: Int
    val battery: Int?
}

@Serializable
data class MotionSensorUpdate(
    // Generic
    override val linkquality: Int,
    override val battery: Int? = null,

    // Light sensor
    val voltage: Int = 0,

    val illuminance: Int = 0,
    val illuminance_lux: Int = 0,
    val occupancy: Boolean = false
) : ZigbeeUpdate

class RelayMqtt(val client: MqttClient, val relayClient: RelayClient, val sceneController: SceneController, val api: Api) {
    companion object : KLogging()

    private fun debugInfo(s: String) {
        if (debugMqtt) {
            logger.debug(s)
        }
    }

    init {
        client.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable) {
                logger.warn { "connectionLost" }
                cause.printStackTrace()
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val host = topic.substring(0, topic.indexOf("/"))

                if (host == "zigbee") {
                    val deviceId = topic.substring(host.length + 1)

                    // Check this is a message isn't from the bridge
                    if (!deviceId.startsWith("0x") || deviceId.contains('/')) return

                    val updateRaw = alleyJson.parseToJsonElement(message.toString())
                    debugInfo("Received zigbee message, device: $deviceId, data: $updateRaw")

                    if (updateRaw is JsonObject && updateRaw.containsKey("motor_state")) {
                        relayClient.getBlind(deviceId).handleMessage(updateRaw)
                    } else if (relayClient.isZPlug(deviceId)) {
                        relayClient.getZPlug(deviceId).handleMessage(updateRaw)
                    } else if (sceneController.zswitches.containsKey(deviceId)) {
                        sceneController.zswitches[deviceId]?.handleMessage(updateRaw)
                    } else {
                        val update = alleyJson.decodeFromJsonElement<MotionSensorUpdate>(updateRaw)

                        if (update.occupancy) {
                            runBlocking {
                                sceneController.onChange(deviceId)
                            }
                        }

                        api.onPropertyChange(PropertyData(deviceId, "illuminance", update.illuminance.toDouble()))
                        update.battery?.let {
                            api.onPropertyChange(PropertyData(deviceId, "battery", it.toDouble()))
                        }
                        api.onPropertyChange(PropertyData(deviceId, "voltage", update.voltage.toDouble()))
                    }
                } else {
                    relayClient.getRelay(host).handleMessage(topic, message)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                debugInfo("deliveryComplete")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                debugInfo("connectComplete")
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
    private val plugMap = mutableMapOf<String, ZPlug>()

    fun getRelay(host: String) = relayMap.getOrPut(host.uppercase()) {
        Relay(host, client, config.relay.apiKey, mqtt)
    }

    fun getBlind(deviceId: String) = blindMap.getOrPut(deviceId) {
        Blind(deviceId, mqtt)
    }

    fun getZPlug(deviceId: String) = plugMap.getOrPut(deviceId) {
        ZPlug(deviceId, mqtt)
    }

    fun isRelay(deviceId: String) = relayMap.containsKey(deviceId)
    fun isBlind(deviceId: String) = blindMap.containsKey(deviceId)
    fun isZPlug(deviceId: String) = plugMap.containsKey(deviceId)
}
