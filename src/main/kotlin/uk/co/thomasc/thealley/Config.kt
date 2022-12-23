package uk.co.thomasc.thealley

import io.ktor.application.Application

fun Application.config() = environment.config.config("thealley").let {
    Config(
        it.config("relay").let { rCfg ->
            Config.RelayConfig(
                rCfg.propertyOrNull("apiKey")?.getString() ?: ""
            )
        },
        it.config("tado").let { tCfg ->
            Config.TadoConfig(
                tCfg.propertyOrNull("refreshToken")?.getString() ?: ""
            )
        },
        it.config("mqtt").let { mCfg ->
            Config.MqttConfig(
                mCfg.propertyOrNull("host")?.getString() ?: "",
                mCfg.propertyOrNull("user")?.getString() ?: "",
                mCfg.propertyOrNull("pass")?.getString() ?: ""
            )
        }
    )
}

data class Config(
    val relay: RelayConfig = RelayConfig(),
    val tado: TadoConfig = TadoConfig(),
    val mqtt: MqttConfig = MqttConfig()
) {

    data class MqttConfig(
        var clientId: String = "thealley",
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
