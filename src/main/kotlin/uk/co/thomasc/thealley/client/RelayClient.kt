package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.context.annotation.Bean
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import uk.co.thomasc.thealley.Config
import uk.co.thomasc.thealley.devices.Relay


data class RelayState(@JsonAlias("relay/0") val relay0: Boolean)

@Component
class RelayMqtt(val config: Config) {
    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {
        val clientFactory = DefaultMqttPahoClientFactory()
        clientFactory.setServerURIs("tcp://${config.mqtt.host}:1883")
        clientFactory.setUserName(config.mqtt.user)
        clientFactory.setPassword(config.mqtt.pass)
        return clientFactory
    }

    @Bean
    fun mqttOutboundChannel(): MessageChannel {
        return DirectChannel()
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    fun mqttOutbound(): MessageHandler {
        val messageHandler = MqttPahoMessageHandler("thealley", mqttClientFactory())
        messageHandler.setAsync(true)
        messageHandler.setDefaultTopic("default")
        messageHandler.setDefaultQos(1)
        return messageHandler
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    interface DeviceGateway {
        fun sendToMqtt(payload: Message<String>)
    }
}

@Component
class RelayClient(val restTemplate: RestTemplate, val config: Config, val mqtt: RelayMqtt.DeviceGateway) {

    fun getRelay(host: String) = Relay(host, restTemplate, config.relay.apiKey, mqtt)

}
