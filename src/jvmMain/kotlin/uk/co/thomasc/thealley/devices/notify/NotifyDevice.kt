package uk.co.thomasc.thealley.devices.notify

import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
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

@Serializable
data class ChatRequest(val chatId: String, val text: String, val session: String)

class NotifyDevice(id: Int, config: NotifyConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<NotifyDevice, NotifyConfig, EmptyState>(id, config, state, stateStore) {

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
                }
            }
        }
    }

    private suspend fun sendNotification(text: String) {
        config.users.forEach { u ->
            client.post("${config.baseUrl}/api/sendText") {
                contentType(ContentType.Application.Json)
                setBody(ChatRequest("$u@c.us", text, config.session))
            }
        }
    }

    companion object : KLogging() {
        val threadPool = newFixedThreadPoolContext(3, "Websocket")
    }
}
