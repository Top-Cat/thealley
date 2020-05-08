package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.core.MessageProducer
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import uk.co.thomasc.thealley.Config
import uk.co.thomasc.thealley.devices.Relay
import uk.co.thomasc.thealley.rest.Api
import uk.co.thomasc.thealley.rest.PropertyData
import uk.co.thomasc.thealley.scenes.SceneController

data class RelayState(@JsonAlias("relay/0") val relay0: Boolean)
data class ZigbeeUpdate(val illuminance: Int, val linkquality: Int, val occupancy: Boolean?, val battery: Int?, val voltage: Int?, val illuminance_lux: Int?)

@Configuration
@EnableIntegration
class RelayMqtt(val config: Config, val relayClient: RelayClient, val sceneController: SceneController, val api: Api) {
    val mqttClientFactory by lazy {
        DefaultMqttPahoClientFactory().apply {
            connectionOptions = MqttConnectOptions().apply {
                serverURIs = arrayOf("tcp://${config.mqtt.host}:1883")
                userName = config.mqtt.user
                password = config.mqtt.pass.toCharArray()
                maxInflight = 50
            }
        }
    }

    val mapper = jacksonObjectMapper()

    @Bean
    fun mqttOutboundChannel(): MessageChannel = DirectChannel()

    @Bean
    fun mqttInputChannel(): MessageChannel = DirectChannel()

    @Bean
    fun inbound(): MessageProducer =
        MqttPahoMessageDrivenChannelAdapter("thealleyIn", mqttClientFactory, "#").apply {
            //setCompletionTimeout(5000)
            setConverter(DefaultPahoMessageConverter())
            setQos(1)
            outputChannel = mqttInputChannel()
        }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    fun mqttOutbound() =
        MqttPahoMessageHandler("thealleyOut", mqttClientFactory).apply {
            setAsync(true)
            setDefaultTopic("default")
            setDefaultQos(1)
        }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    interface DeviceGateway {
        fun sendToMqtt(payload: Message<String>)
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    fun handler(): MessageHandler =
        MessageHandler {
            message ->

            val topic = message.headers.getValue(MqttHeaders.RECEIVED_TOPIC) as String
            val host = topic.substring(0, topic.indexOf("/"))

            if (host == "zigbee") {
                val deviceId = topic.substring(host.length + 1)

                // Check this is a message isn't from the bridge
                if (!deviceId.startsWith("0x")) return@MessageHandler

                val update = mapper.readValue<ZigbeeUpdate>(message.payload as String)
                println("Received zigbee message, device: $deviceId, data: $update")

                if (update.occupancy == true) {
                    sceneController.onChange(deviceId)
                }
                api.onPropertyChange(PropertyData(deviceId, "illuminance", update.illuminance.toDouble()))
                update.battery?.let {
                    api.onPropertyChange(PropertyData(deviceId, "battery", it.toDouble()))
                }
                update.voltage?.let {
                    api.onPropertyChange(PropertyData(deviceId, "voltage", it.toDouble()))
                }
            } else {
                relayClient.getRelay(host).handleMessage(message)
            }
        }
}

@Component
class RelayClient(val restTemplate: RestTemplate, val config: Config, val mqtt: RelayMqtt.DeviceGateway) {

    private val relayMap = mutableMapOf<String, Relay>()

    fun getRelay(host: String) = relayMap.getOrPut(host.toUpperCase()) {
        Relay(host, restTemplate, config.relay.apiKey, mqtt)
    }

}
