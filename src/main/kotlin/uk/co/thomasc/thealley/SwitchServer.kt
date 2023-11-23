package uk.co.thomasc.thealley

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.connection
import io.ktor.network.sockets.isClosed
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.server.application.ApplicationStopping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.scenes.SceneController
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

class SwitchServer(
    private val sceneController: SceneController,
    private val mqtt: RelayMqtt.DeviceGateway,
    private val environment: ApplicationEnvironment
) {
    companion object {
        val threadPool = newFixedThreadPoolContext(10, "SwitchServer")
        val selector = ActorSelectorManager(Dispatchers.IO)
    }

    private val server: ServerSocket =
        aSocket(selector).tcp().bind(port = 5558)

    init {
        GlobalScope.launch(threadPool) {
            listenForClients()
        }.apply {
            environment.monitor.subscribe(ApplicationStopPreparing) {
                cancel()
                runBlocking {
                    join()
                }
            }

            invokeOnCompletion {
                server.close()
            }
        }
    }

    private suspend fun listenForClients() {
        while (true) {
            val client = server.accept()
            coroutineScope {
                launch {
                    val job = with(SwitchClient(sceneController, client, mqtt)) {
                        launch { client.use { run() } } to launch { keepalive() }
                    }

                    environment.monitor.subscribe(ApplicationStopping) {
                        job.first.cancel()
                        job.second.cancel()
                        runBlocking {
                            job.first.join()
                            job.second.join()
                        }
                    }
                }
            }
        }
    }
}

class SwitchClient(
    private val sceneController: SceneController,
    private val client: Socket,
    private val mqtt: RelayMqtt.DeviceGateway
) {
    companion object : KLogging() {
        const val keepaliveInterval = 10
    }

    private val conn = client.connection()

    suspend fun keepalive() {
        try {
            while (true) {
                delay(keepaliveInterval * 1000L)
                with(conn.output) {
                    writeByte(51)
                    flush()
                }
            }
        } catch (e: IOException) {
            // do nothing
        }
    }

    suspend fun run() {
        logger.info { "Client connected: ${client.remoteAddress}" }
        val bb = ByteBuffer.allocate(32)
        val q = ArrayBlockingQueue<Int>(128)

        try {
            while (true) {
                bb.clear()

                if (conn.input.readAvailable(bb) == -1) {
                    throw IOException()
                }

                bb.flip()
                while (bb.hasRemaining()) {
                    q.add(bb.get().toInt())
                }

                while (q.size > 3) {
                    when (q.poll()) {
                        52 -> {
                            val switchId = q.poll()
                            val buttonId = q.poll()
                            val buttonState = q.poll()

                            sceneController.switches[switchId to buttonId]?.let {
                                when (buttonState) {
                                    1 -> it.toggle()
                                    2 -> it.revoke()
                                    3 -> it.startFade()
                                    4 -> it.endFade()
                                    else -> logger.warn { "Unknown state $buttonState" }
                                }
                            } ?: logger.warn { "Unknown switch $switchId/$buttonId" }
                        }
                        53 -> {
                            val dataType = q.poll()
                            // Read signed short
                            val value = q.poll().toUByte().toInt() or (q.poll().toInt() shl 8)

                            mqtt.sendToMqtt("sensor/multi/$dataType", MqttMessage("$value".toByteArray()))
                        }
                    }
                }
            }
        } catch (e: IOException) {
            logger.info { "Client disconnected: ${client.remoteAddress}" }
        } finally {
            logger.info { "Closing ${client.remoteAddress}" }
            client.close()
            logger.info { "Finally: ${client.remoteAddress}, ${client.isClosed}" }
        }
    }
}
