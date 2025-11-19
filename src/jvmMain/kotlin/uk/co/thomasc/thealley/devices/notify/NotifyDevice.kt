package uk.co.thomasc.thealley.devices.notify

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.Serializable
import mu.KLogging
import uk.co.thomasc.thealley.client
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.events.TexecomAreaEvent
import uk.co.thomasc.thealley.devices.energy.tado.TadoCodeEvent
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.types.NotifyConfig

@Serializable
data class NtfyPub(
    val topic: String,
    val message: String? = null,
    val title: String? = null,
    val tags: List<String>? = null,
    val priority: Int? = null,
    val actions: List<NtfyAction>? = null,
    val click: String? = null,
    val attach: String? = null,
    val markdown: Boolean? = null,
    val icon: String? = null,
    val filename: String? = null,
    val delay: String? = null,
    val email: String? = null,
    val call: String? = null
)

interface NtfyAction {
    val action: String
    val label: String
    val clear: Boolean?
}

@Serializable
data class NtfyViewAction(
    override val label: String,
    val url: String,
    override val clear: Boolean? = null
) : NtfyAction {
    override val action = "view"
}

@Serializable
data class NtfyBroadcastAction(
    override val label: String,
    val intent: String? = null,
    val extras: Map<String, String>? = null,
    override val clear: Boolean? = null
) : NtfyAction {
    override val action = "broadcast"
}

@Serializable
data class NtfyHttpAction(
    override val label: String,
    val url: String,
    val method: String? = null,
    val headers: Map<String, String>? = null,
    val body: String? = null,
    override val clear: Boolean? = null
) : NtfyAction {
    override val action = "http"
}

class NotifyDevice(id: Int, config: NotifyConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<NotifyDevice, NotifyConfig, EmptyState>(id, config, state, stateStore) {

    private val channel = Channel<suspend (HttpClient) -> Unit>(10)

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<TexecomAreaEvent> {
            sendNotification("Area ${it.status.human}", "Area '${it.areaName}'", it.status.tag)
        }

        bus.handle<TadoCodeEvent> {
            sendNotification("Tado Authentication Required", "Visit ${it.response.verificationUriComplete} to complete tado login for ${it.account}", "key")
        }

        CoroutineScope(threadPool).launch {
            while (true) {
                channel.receive().invoke(client)
            }
        }
    }

    private fun sendNotification(title: String, message: String, tag: String) {
        channel.trySend {
            it.post(config.baseUrl) {
                contentType(ContentType.Application.Json)
                bearerAuth(config.token)
                setBody(NtfyPub(config.topic, message, title, listOf(tag), actions = emptyList(), icon = "https://topc.at/images/tc.png"))
            }
        }
    }

    companion object : KLogging() {
        val threadPool = newFixedThreadPoolContext(3, "Websocket")
    }
}
