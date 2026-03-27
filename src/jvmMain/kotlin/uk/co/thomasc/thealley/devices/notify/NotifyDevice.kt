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
import mu.KLogging
import uk.co.thomasc.thealley.client
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.events.TexecomAreaEvent
import uk.co.thomasc.thealley.devices.energy.tado.TadoCodeEvent
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.types.NotifyConfig
import uk.co.thomasc.thealley.devices.zigbee.custom.LowBatteryEvent

class NotifyDevice(id: Int, config: NotifyConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>, val dev: AlleyDeviceMapper) :
    AlleyDevice<NotifyDevice, NotifyConfig, EmptyState>(id, config, state, stateStore) {

    private val channel = Channel<suspend (HttpClient) -> Unit>(10)

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<TexecomAreaEvent> {
            sendNotification("Area ${it.status.human}", "Area '${it.areaName}'", it.status.tag)
        }

        bus.handle<TadoCodeEvent> {
            sendNotification("Tado Authentication Required", "Visit ${it.response.verificationUriComplete} to complete tado login for ${it.account}", "key")
        }

        bus.handle<LowBatteryEvent> {
            val device: AlleyDevice<*, *, *> = dev.getDevice(it.deviceId) ?: throw Exception("Device not found")
            sendNotification("${device.config.name} has low battery", "${device.config.name} battery level is at ${it.batteryLevel}", "battery")
        }

        CoroutineScope(threadPool).launch {
            while (true) {
                try {
                    channel.receive().invoke(client)
                } catch (e: Exception) {
                    logger.error(e) { "Error handing notification" }
                }
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
