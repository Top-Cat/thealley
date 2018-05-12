package uk.co.thomasc.thealley.client

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import uk.co.thomasc.thealley.devices.Light
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import uk.co.thomasc.thealley.Config
import org.springframework.context.annotation.Bean
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.support.MessageBuilder


data class RelayState(val relay0: Boolean)

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

class Relay(
    private val host: String,
    private val restTemplate: RestTemplate,
    private val apiKey: String,
    private val mqtt: RelayMqtt.DeviceGateway
) : Light<Unit> {

    override fun setPowerState(value: Boolean) =
        setLightState(if (value) 1 else 0)

    override fun setComplexState(brightness: Int?, hue: Int?, saturation: Int?, temperature: Int?, transitionTime: Int?) {
        brightness?.let {
            setPowerState(brightness > 50)
        }
    }

    private fun setLightState(state: Int) {
        mqtt.sendToMqtt(MessageBuilder.withPayload("$state").setHeader(MqttHeaders.TOPIC, "$host/relay/0/set").build())
    }

    fun getState(): Boolean {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val request = HttpEntity<MultiValueMap<String, String>>(headers)
        return restTemplate.exchange(
            "http://$host.light.kirkstall.top-cat.me/api/relay/0?apikey=$apiKey",
            HttpMethod.GET,
            request,
            RelayState::class.java
        ).body?.relay0 ?: false
    }

}
