package uk.co.thomasc.thealley.devices.system.mqtt

import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.types.MqttConfig

class MqttDevice(id: Int, config: MqttConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<MqttDevice, MqttConfig, EmptyState>(id, config, state, stateStore) {

    private val connectionOptions = MqttConnectOptions().apply {
        serverURIs = arrayOf("tcp://${config.host}:1883")
        userName = config.user
        password = config.pass.toCharArray()
        maxInflight = 50
        isAutomaticReconnect = true
    }
    private val client = MqttClient("tcp://${config.host}:1883", config.clientId + suffix)

    override suspend fun init(bus: AlleyEventBusShim) {
        client.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable) {
                logger.warn { "connectionLost" }
                cause.printStackTrace()
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                logger.debug { "Received zigbee message, device: $topic, data: $message" }

                runBlocking {
                    bus.emit(MqttMessageEvent(topic, message.toString()))
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                logger.debug { "deliveryComplete" }
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                logger.info { "connectComplete" }
                client.subscribe("#")
            }
        })

        client.connect(connectionOptions)

        bus.handle<MqttSendEvent> {
            client.publish(it.topic, MqttMessage(it.payload.toByteArray()))
        }
    }

    companion object : KLogging() {
        val suffix = System.getenv("MQTT_SUFFIX") ?: ""
    }
}
