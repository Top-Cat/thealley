package uk.co.thomasc.thealley

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import uk.co.thomasc.thealley.oauth.AlleyTokenStore

private fun setupClient(block: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {}) = HttpClient(Apache) {
    install(HttpTimeout)
    install(ContentNegotiation) {
        json(alleyJson)
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(alleyJson)
        pingInterval = 20_000
    }

    engine {
        customizeClient {
            setMaxConnTotal(100)
            setMaxConnPerRoute(20)
        }
    }

    block()
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkOauth(alleyTokenStore: AlleyTokenStore, block: suspend (String) -> Unit) {
    val authHeader = call.request.parseAuthorizationHeader()
    if (authHeader is HttpAuthHeader.Single) {
        val token = alleyTokenStore.accessToken(authHeader.blob)

        when (token?.expired()) {
            true -> alleyTokenStore.revokeAccessToken(token.accessToken).let { null }
            false -> block(token.identity?.username ?: "").let { true }
            null -> null
        }
    } else {
        null
    } ?: call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
}

val client = setupClient()
