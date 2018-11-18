package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.annotation.JsonAlias
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
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.devices.Relay

data class RelayState(@JsonAlias("relay/0") val relay0: Boolean)

@Configuration
@EnableIntegration
class RelayMqtt(val config: Config, val relayClient: RelayClient) {
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

            relayClient.getRelay(host).handleMessage(message)
        }
}

@Component
class RelayClient(val restTemplate: RestTemplate, val config: Config, val mqtt: RelayMqtt.DeviceGateway) {

    private val relayMap = mutableMapOf<String, Relay>()

    fun getRelay(host: String) = relayMap.getOrPut(host.toUpperCase()) {
        Relay(host, restTemplate, config.relay.apiKey, mqtt)
    }

}
