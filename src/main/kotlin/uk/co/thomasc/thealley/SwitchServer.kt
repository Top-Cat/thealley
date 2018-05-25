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
import java.net.InetSocketAddress
import java.nio.ByteBuffer

@Component
class SwitchServer(
    val kasa: LocalClient,
    val relayClient: RelayClient,
    val switchRepo: SwitchRepository
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
                    SwitchClient(kasa, relayClient, switchRepo, it).run()
                }
            }
        }
    }

}

class SwitchClient(
    kasa: LocalClient,
    relayClient: RelayClient,
    private val switchRepo: SwitchRepository,
    private val client: Socket
) : DeviceMapper(kasa, relayClient, switchRepo) {

    suspend fun run() {
        println("Client connected: ${client.remoteAddress}")
        val bb = ByteBuffer.allocate(32)

        while (true) {
            bb.clear()

            if (client.read(bb) == -1) {
                println("Client disconnected: ${client.remoteAddress}")
                return
            }

            bb.flip()

            while (bb.hasRemaining()) {
                when (bb.get().toInt()) {
                    52 -> {
                        val switchId = bb.get()
                        val buttonId = bb.get()
                        val buttonState = bb.get()

                        println("Button pressed - $switchId - $buttonId - $buttonState")
                    }
                }
            }
        }
    }

}
