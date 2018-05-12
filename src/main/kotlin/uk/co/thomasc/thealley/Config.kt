package uk.co.thomasc.thealley

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "thealley")
class Config {
    val relay = RelayConfig()
    val tado = TadoConfig()
    val mqtt = MqttConfig()

    class MqttConfig {
        lateinit var host: String
        lateinit var user: String
        lateinit var pass: String
    }

    class RelayConfig {
        lateinit var apiKey: String
    }

    class TadoConfig {
        lateinit var refreshToken: String
    }
}
