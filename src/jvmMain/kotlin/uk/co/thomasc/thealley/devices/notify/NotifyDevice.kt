package uk.co.thomasc.thealley.devices.notify

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import uk.co.thomasc.thealley.client
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.events.TexecomAreaEvent
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.types.NotifyConfig

data class ChatRequest(val chatId: String, val text: String, val session: String)

class NotifyDevice(id: Int, config: NotifyConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<NotifyDevice, NotifyConfig, EmptyState>(id, config, state, stateStore) {

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<TexecomAreaEvent> {
            sendNotification("Area '${it.areaName}' ${it.status}")
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
}
