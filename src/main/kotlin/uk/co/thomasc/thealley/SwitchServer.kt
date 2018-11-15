package uk.co.thomasc.thealley

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.sockets.ServerSocket
import kotlinx.sockets.Socket
import kotlinx.sockets.aSocket
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.scenes.SceneController
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

@Component
class SwitchServer(
    val sceneController: SceneController,
    val mqtt: RelayMqtt.DeviceGateway
) {

    var server: ServerSocket =
        aSocket().tcp().bind(InetSocketAddress(5558))

    init {
        launch (CommonPool) {
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
            launch(CommonPool) {
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
        val bb = ByteBuffer.allocate(32)
        val q = ArrayBlockingQueue<Int>(128)

        try {
            while (true) {
                bb.clear()

                if (client.read(bb) == -1) {
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
                            }
                        }
                        53 -> {
                            val dataType = q.poll()
                            // Read signed short
                            val value = q.poll().toUByte().toInt() or (q.poll().toInt() shl 8)

                            mqtt.sendToMqtt(MessageBuilder.withPayload("$value").setHeader(MqttHeaders.TOPIC, "sensor/multi/$dataType").build())
                        }
                    }
                }
            }
        } catch (e: IOException) {
            println("Client disconnected: ${client.remoteAddress}")
        }
    }

}
