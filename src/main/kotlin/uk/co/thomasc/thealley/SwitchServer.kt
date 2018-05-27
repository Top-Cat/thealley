package uk.co.thomasc.thealley

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.sockets.ServerSocket
import kotlinx.sockets.Socket
import kotlinx.sockets.aSocket
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.repo.SwitchRepository
import uk.co.thomasc.thealley.scenes.SceneController
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

@Component
class SwitchServer(
    val kasa: LocalClient,
    val relayClient: RelayClient,
    val switchRepo: SwitchRepository,
    val sceneController: SceneController
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
                    SwitchClient(kasa, relayClient, switchRepo, sceneController, it).run()
                }
            }
        }
    }

}

class SwitchClient(
    kasa: LocalClient,
    relayClient: RelayClient,
    switchRepo: SwitchRepository,
    private val sceneController: SceneController,
    private val client: Socket
) : DeviceMapper(kasa, relayClient, switchRepo) {

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
                    }
                }
            }
        } catch (e: IOException) {
            println("Client disconnected: ${client.remoteAddress}")
        }
    }

}
