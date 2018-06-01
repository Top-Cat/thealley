package uk.co.thomasc.thealley.devices

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.client.RelayState

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

    override fun getPowerState(): Boolean {
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

    override fun togglePowerState() = setLightState(2)

}
