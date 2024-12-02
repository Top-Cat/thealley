package uk.co.thomasc.thealley.devices.notify

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.Serializable
import mu.KLogging
import uk.co.thomasc.thealley.client
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.events.TexecomAreaEvent
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.types.NotifyConfig
import uk.co.thomasc.thealley.websocketClient

interface WahaRequest {
    val session: String
}

interface WahaChatRequest : WahaRequest {
    val chatId: String
}

@Serializable
data class ChatRequest(override val chatId: String, val text: String, override val session: String) : WahaChatRequest

@Serializable
data class SetSeenRequest(override val chatId: String, val messageId: String, override val session: String) : WahaChatRequest

@Serializable
data class TypingRequest(override val chatId: String, override val session: String) : WahaChatRequest

class NotifyDevice(id: Int, config: NotifyConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<NotifyDevice, NotifyConfig, EmptyState>(id, config, state, stateStore) {

    private val channel = Channel<suspend (HttpClient) -> Unit>(Channel.Factory.RENDEZVOUS)

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<TexecomAreaEvent> {
            sendNotification("Area '${it.areaName}' ${it.status}")
        }

        CoroutineScope(threadPool).launch {
            websocketClient.webSocket(
                method = HttpMethod.Get,
                host = "waha",
                port = 80,
                path = "/ws?session=${config.session}&events=message"
            ) {
                while (true) {
                    val othersMessage = receiveDeserialized<WahaEvent<WahaMessage>>()
                    logger.info { "Received websocket message: $othersMessage" }
                    setSeen(othersMessage.payload.from, othersMessage.payload.id)
                }
            }
        }

        CoroutineScope(threadPool).launch {
            while (true) {
                channel.receive().invoke(client)
                delay(500L)
            }
        }
    }

    private suspend fun setSeen(user: String, id: String) {
        channel.send {
            it.post("${config.baseUrl}/api/sendSeen") {
                contentType(ContentType.Application.Json)
                setBody(SetSeenRequest(user, id, config.session))
            }
        }
    }

    private suspend fun sendNotification(text: String) {
        logger.info { "Sending notification: $text" }
        config.users.map { u -> "$u@c.us" }.forEach { u ->
            channel.send {
                it.post("${config.baseUrl}/api/startTyping") {
                    contentType(ContentType.Application.Json)
                    setBody(TypingRequest(u, config.session))
                }
            }

            delay(500L)

            channel.send {
                val response = it.post("${config.baseUrl}/api/sendText") {
                    contentType(ContentType.Application.Json)
                    setBody(ChatRequest(u, text, config.session))
                }

                val body = response.bodyAsText()

                logger.info { "Notification sent: (${response.status}) $body" }
            }

            channel.send {
                it.post("${config.baseUrl}/api/stopTyping") {
                    contentType(ContentType.Application.Json)
                    setBody(TypingRequest(u, config.session))
                }
            }
        }
    }

    companion object : KLogging() {
        val threadPool = newFixedThreadPoolContext(3, "Websocket")
    }
}
