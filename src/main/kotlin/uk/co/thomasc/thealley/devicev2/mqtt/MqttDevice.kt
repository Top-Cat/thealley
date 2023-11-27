package uk.co.thomasc.thealley.devicev2.mqtt

import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.EmptyState
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.MqttConfig

class MqttDevice(config: MqttConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<MqttDevice, MqttConfig, EmptyState>(config, state, stateStore) {

    private val connectionOptions = MqttConnectOptions().apply {
        serverURIs = arrayOf("tcp://${config.host}:1883")
        userName = config.user
        password = config.pass.toCharArray()
        maxInflight = 50
        isAutomaticReconnect = true
    }
    private val client = MqttClient("tcp://${config.host}:1883", config.clientId)

    private val sender = object : RelayMqtt.DeviceGateway {
        override fun sendToMqtt(topic: String, payload: MqttMessage) {
            client.publish(topic, payload)
        }
    }

    override suspend fun init(bus: AlleyEventBus) {
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
                logger.debug { "connectComplete" }
                client.subscribe("#")
            }
        })

        client.connect(connectionOptions)

        bus.handle<MqttSendEvent> {
            sender.sendToMqtt(it.topic, MqttMessage(it.payload.toByteArray()))
        }
    }

    companion object : KLogging()
}
