package uk.co.thomasc.thealley.devicev2.switch

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.connection
import io.ktor.network.sockets.isClosed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KLogging
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.AlleyEventEmitter
import uk.co.thomasc.thealley.devicev2.EmptyState
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.ShutdownEvent
import uk.co.thomasc.thealley.devicev2.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devicev2.types.SwitchServerConfig
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import kotlin.coroutines.coroutineContext

class SwitchServerDevice(id: Int, config: SwitchServerConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<SwitchServerDevice, SwitchServerConfig, EmptyState>(id, config, state, stateStore) {

    private val server: ServerSocket = aSocket(selector).tcp().bind(port = config.port)

    private suspend fun listenForClients(bus: AlleyEventBus) {
        while (true) {
            val client = server.accept()
            with(CoroutineScope(coroutineContext)) {
                launch {
                    val job = with(SwitchClient(bus, client)) {
                        launch { client.use { run() } } to launch { keepalive() }
                    }

                    bus.handle<ShutdownEvent> {
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

    override suspend fun init(bus: AlleyEventBus) {
        GlobalScope.launch(threadPool) {
            listenForClients(bus)
        }.apply {
            bus.handle<ShutdownEvent> {
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

    companion object : KLogging() {
        val threadPool = newFixedThreadPoolContext(10, "SwitchServer")
        val selector = ActorSelectorManager(Dispatchers.IO)
    }

    class SwitchClient(
        private val bus: AlleyEventEmitter,
        private val client: Socket
    ) {
        companion object : KLogging() {
            const val KEEPALIVE_INTERVAL = 10
        }

        private val conn = client.connection()

        suspend fun keepalive() {
            try {
                while (true) {
                    delay(KEEPALIVE_INTERVAL * 1000L)
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

                                bus.emit(SwitchEvent(switchId, buttonId, SwitchEvent.State.fromInt(buttonState)))
                            }

                            53 -> {
                                val dataType = q.poll()
                                // Read signed short
                                val value = q.poll().toUByte().toInt() or (q.poll().toInt() shl 8)

                                bus.emit(MqttSendEvent("sensor/multi/$dataType", "$value"))
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
}
