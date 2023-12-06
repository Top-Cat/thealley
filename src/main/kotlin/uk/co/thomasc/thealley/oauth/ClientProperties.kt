package uk.co.thomasc.thealley.oauth

import io.ktor.server.application.Application

data class ClientProperties(
    var clients: List<ClientProperty> = emptyList()
) {
    data class ClientProperty(
        var clientId: String? = null,
        var secret: String? = null,
        var scopes: List<String> = emptyList()
    )

    companion object {
        fun fromApplication(app: Application) = app.environment.config.configList("clients").let { cl ->
            ClientProperties(
                cl.map {
                    ClientProperty(
                        it.propertyOrNull("clientId")?.getString(),
                        it.propertyOrNull("secret")?.getString(),
                        it.propertyOrNull("scopes")?.getList() ?: emptyList()
                    )
                }
            )
        }
    }
}
