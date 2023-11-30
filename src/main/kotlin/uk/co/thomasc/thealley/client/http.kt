package uk.co.thomasc.thealley.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val alleyJsonUgly = Json

val alleyJson = Json(alleyJsonUgly) {
    prettyPrint = true
}

private fun setupClient(block: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {}) = HttpClient(Apache) {
    install(HttpTimeout)
    install(ContentNegotiation) {
        json(alleyJson)
    }

    engine {
        customizeClient {
            setMaxConnTotal(100)
            setMaxConnPerRoute(20)
        }
    }

    block()
}

val client = setupClient()
