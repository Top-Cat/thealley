package uk.co.thomasc.thealley

import io.ktor.server.application.Application

fun Application.config() = environment.config.config("thealley").let {
    Config(
        it.config("relay").let { rCfg ->
            Config.RelayConfig(
                rCfg.propertyOrNull("apiKey")?.getString() ?: ""
            )
        },
        it.config("tado").let { tCfg ->
            Config.TadoConfig(
                tCfg.propertyOrNull("email")?.getString() ?: "",
                tCfg.propertyOrNull("password")?.getString() ?: "",
                tCfg.propertyOrNull("homeId")?.getString()?.toIntOrNull() ?: 0
            )
        },
        it.config("mqtt").let { mCfg ->
            Config.MqttConfig(
                mCfg.propertyOrNull("clientId")?.getString() ?: "",
                mCfg.propertyOrNull("host")?.getString() ?: "",
                mCfg.propertyOrNull("user")?.getString() ?: "",
                mCfg.propertyOrNull("pass")?.getString() ?: ""
            )
        }
    )
}

data class Config(
    val relay: RelayConfig,
    val tado: TadoConfig,
    val mqtt: MqttConfig
) {
    data class MqttConfig(
        val clientId: String,
        val host: String,
        val user: String,
        val pass: String
    )

    data class RelayConfig(
        val apiKey: String
    )

    data class TadoConfig(
        val email: String,
        val password: String,
        val homeId: Int
    )
}
