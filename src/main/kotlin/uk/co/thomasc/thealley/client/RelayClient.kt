package uk.co.thomasc.thealley.client

import kotlinx.coroutines.experimental.runBlocking
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import uk.co.thomasc.thealley.devices.Light
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.http.HttpEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.http.HttpMethod
import uk.co.thomasc.thealley.Config

@Component
class RelayClient(val restTemplate: RestTemplate, val config: Config) {

    fun getRelay(host: String) = Relay(host, restTemplate, config.relay.apiKey)

}

class Relay(val host: String, val restTemplate: RestTemplate, val apiKey: String) : Light<Unit> {

    override fun setPowerState(value: Boolean) =
        setLightState(if (value) 1 else 0)

    private fun setLightState(state: Int) {
        val bodyMap = LinkedMultiValueMap<String, String>()
        bodyMap["apikey"] = apiKey
        bodyMap["value"] = "$state"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val request = HttpEntity<MultiValueMap<String, String>>(bodyMap, headers)
        restTemplate.exchange("http://$host/api/relay/0", HttpMethod.PUT, request, String::class.java).body
    }

}
