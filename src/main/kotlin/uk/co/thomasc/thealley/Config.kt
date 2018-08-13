package uk.co.thomasc.thealley

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "thealley")
data class Config(
    val relay: RelayConfig = RelayConfig(),
    val tado: TadoConfig = TadoConfig(),
    val mqtt: MqttConfig = MqttConfig()
) {

    data class MqttConfig(
        var host: String = "",
        var user: String = "",
        var pass: String = ""
    )

    data class RelayConfig(
        var apiKey: String = ""
    )

    data class TadoConfig(
        var refreshToken: String = ""
    )
}
