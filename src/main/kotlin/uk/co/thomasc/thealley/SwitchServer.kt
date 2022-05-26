package uk.co.thomasc.thealley

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.scenes.SceneController
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

class SwitchServer(
    val sceneController: SceneController,
    val mqtt: RelayMqtt.DeviceGateway
) {
    companion object {
        val threadPool = newFixedThreadPoolContext(10, "SwitchServer")
        val selector = ActorSelectorManager(Dispatchers.IO)
    }

    var server: ServerSocket =
        aSocket(selector).tcp().bind(InetSocketAddress(5558))

    init {
        GlobalScope.launch(threadPool) {
            listenForClients()
        }.apply {
            invokeOnCompletion {
                server.close()
            }
        }
    }

    suspend fun listenForClients() {
        while (true) {
            val client = server.accept()
            GlobalScope.launch(threadPool) {
                client.use {
                    SwitchClient(sceneController, it, mqtt).run()
                }
            }
        }
    }
}

class SwitchClient(
    private val sceneController: SceneController,
    private val client: Socket,
    val mqtt: RelayMqtt.DeviceGateway
) {

    suspend fun run() {
        println("Client connected: ${client.remoteAddress}")
        val conn = client.connection()
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
                                    else -> println("Unknown state $buttonState")
                                }
                            } ?: println("Unknown switch $switchId/$buttonId")
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
            println("Client disconnected: ${client.remoteAddress}")
        }
    }
}
