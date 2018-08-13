package uk.co.thomasc.thealley.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("clients")
data class ClientProperties(
    var clients: List<ClientProperty> = emptyList()
)

data class ClientProperty(
    var clientId: String? = null,
    var secret: String? = null,
    var scopes: List<String> = emptyList()
)
