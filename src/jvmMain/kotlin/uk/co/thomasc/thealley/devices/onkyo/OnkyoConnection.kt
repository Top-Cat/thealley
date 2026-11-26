package uk.co.thomasc.thealley.devices.onkyo

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.CancellationException
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import mu.KLogging
import uk.co.thomasc.thealley.devices.kasa.KasaDevice
import uk.co.thomasc.thealley.devices.onkyo.packet.IOnkyoResponse
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OnkyoConnection(private val host: String, private val port: Int = 60128) : Closeable {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var inputChannel: ByteReadChannel
    private lateinit var outputChannel: ByteWriteChannel
    private lateinit var packetChannel: Channel<IOnkyoResponse>
    private val handlers = mutableMapOf<KClass<out IOnkyoResponse>, MutableList<OnkyoPacketHandler<*>>>()

    private val state = MutableStateFlow<Socket?>(null)

    fun init() = connect()

    inline fun <reified T : IOnkyoResponse> handle(block: OnkyoPacketHandler<T>) = handle(T::class, block)

    fun <T : IOnkyoResponse> handle(kClass: KClass<T>, block: OnkyoPacketHandler<T>) {
        handlers.getOrPut(kClass) { mutableListOf() }.add(block)
    }

    suspend fun await(timeout: Duration): Socket? =
        withTimeoutOrNull(timeout) { state.filterNotNull().first() }

    private fun connect() {
        scope.launch {
            var backoff = 0
            var socket: Socket? = null
            val globalCtx = currentCoroutineContext()
            while (globalCtx.isActive) {
                try {
                    delay(backoff.seconds)
                    logger.info { "Connecting to $host:$port" }

                    packetChannel = Channel(0)
                    socket = aSocket(KasaDevice.selector).tcp().connect(host, port).apply {
                        inputChannel = openReadChannel()
                        outputChannel = openWriteChannel(true)
                    }

                    logger.info { "Connected" }
                    backoff = 0
                    state.value = socket

                    receiveLoop()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    packetChannel.close(e)

                    if (state.value != null) {
                        logger.error(e) { "Failed to connect to $host" }
                        if (backoff < 120) backoff += 5
                    } else {
                        logger.warn(e) { "Failure reading packet, reconnecting" }
                    }
                } finally {
                    state.compareAndSet(socket, null)
                    withContext(Dispatchers.IO) {
                        runCatching {
                            socket?.close()
                        }
                    }
                }
            }
        }
    }

    private suspend fun read(bytes: Int) =
        ByteArray(bytes).also {
            val localBuff = ByteBuffer.wrap(it)
            while (localBuff.position() < localBuff.limit()) {
                inputChannel.readAvailable(localBuff)
            }
        }

    private suspend inline fun <reified T : IOnkyoResponse> emit(packet: T) =
        handlers[packet::class]?.filterIsInstance<OnkyoPacketHandler<T>>()?.forEach { it.invoke(packet) }

    private suspend fun receiveLoop() {
        while (state.value?.isClosed == false) {
            val magic = read(4).toString(Charset.defaultCharset())
            magic == "ISCP" || throw OnkyoParseException("Bad magic")

            val headerLength = inputChannel.readInt()
            val contentLength = inputChannel.readInt()

            val version = inputChannel.readByte()
            version == 1.toByte() || throw OnkyoParseException("Bad version")

            read(headerLength - 13)

            val content = read(contentLength)

            Packet(content).typed()?.let { typed ->
                logger.debug { "Received $typed" }
                val result = packetChannel.trySend(typed)
                if (result.isFailure && handlers.containsKey(typed::class)) {
                    emit(typed)
                }
            }
        }
    }

    suspend inline fun <reified T : IOnkyoResponse> send(p: IOnkyoResponse): T? =
        withTimeoutOrNull(600.milliseconds) {
            logger.debug { "Sending $p" }
            send(p.toPacket()) as? T
        }

    suspend fun sendOnly(p: IOnkyoResponse) = sendOnly(p.toPacket())

    suspend fun sendOnly(p: Packet) {
        val buff = ByteBuffer.wrap(p.bytes())
        outputChannel.writeFully(buff)
    }

    suspend fun send(p: Packet): IOnkyoResponse {
        sendOnly(p)
        return receive()
    }

    private suspend fun receive() = packetChannel.receive().also {
        logger.debug { "Handling $it" }
    }

    override fun close() {
        state.value?.close()
    }

    companion object : KLogging()
}
