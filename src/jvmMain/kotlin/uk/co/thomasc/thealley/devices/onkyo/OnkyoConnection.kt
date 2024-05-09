package uk.co.thomasc.thealley.devices.onkyo

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mu.KLogging
import uk.co.thomasc.thealley.devices.kasa.KasaDevice
import uk.co.thomasc.thealley.devices.onkyo.packet.IOnkyoResponse
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.reflect.KClass

class OnkyoConnection(private val host: String, private val port: Int = 60128) : Closeable {
    private lateinit var conn: Socket
    private lateinit var inputChannel: ByteReadChannel
    private lateinit var outputChannel: ByteWriteChannel
    private lateinit var packetChannel: Channel<IOnkyoResponse>
    private val handlers = mutableMapOf<KClass<out IOnkyoResponse>, MutableList<OnkyoPacketHandler<*>>>()

    suspend fun init() = connect()

    inline fun <reified T : IOnkyoResponse> handle(block: OnkyoPacketHandler<T>) = handle(T::class, block)

    fun <T : IOnkyoResponse> handle(kClass: KClass<T>, block: OnkyoPacketHandler<T>) {
        handlers.getOrPut(kClass) { mutableListOf() }.add(block)
    }

    private suspend fun connect() {
        logger.info { "Connecting to $host:$port" }

        packetChannel = Channel(0)
        conn = aSocket(KasaDevice.selector).tcp().connect(host, port).apply {
            inputChannel = openReadChannel()
            outputChannel = openWriteChannel(true)
        }

        GlobalScope.launch {
            try {
                receiveLoop()
            } catch (e: Exception) {
                logger.warn(e) { "Failure reading packet, reconnecting" }
                conn.close()
                packetChannel.close(e)
                connect()
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
        while (!conn.isClosed) {
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
        withTimeoutOrNull(600) {
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
        conn.close()
    }

    companion object : KLogging()
}
