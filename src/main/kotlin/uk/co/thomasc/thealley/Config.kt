package uk.co.thomasc.thealley

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "thealley")
class Config {
    val relay = RelayConfig()

    class RelayConfig {
        lateinit var apiKey: String
    }
}
