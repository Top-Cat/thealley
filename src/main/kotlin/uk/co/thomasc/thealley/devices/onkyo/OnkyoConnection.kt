package uk.co.thomasc.thealley.devices.onkyo

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.core.Closeable
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mu.KLogging
import uk.co.thomasc.thealley.devices.kasa.KasaDevice
import uk.co.thomasc.thealley.devices.onkyo.packet.IOnkyoResponse
import java.nio.ByteBuffer
import java.nio.charset.Charset

class OnkyoConnection(private val host: String, private val port: Int = 60128) : Closeable {
    private lateinit var conn: Socket
    private lateinit var inputChannel: ByteReadChannel
    private lateinit var outputChannel: ByteWriteChannel
    private lateinit var packetChannel: Channel<IOnkyoResponse>

    suspend fun init() {
        // I like big buffers and I cannot lie. Can't work out how to make this non-global
        System.setProperty("io.ktor.utils.io.BufferSize", "10240")

        connect()
    }

    private suspend fun connect() {
        logger.info { "Connecting to $host:$port" }

        packetChannel = Channel(10)
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

        // Consume art packet
        receive()
    }

    private suspend fun read(bytes: Int) =
        ByteArray(bytes).also {
            val localBuff = ByteBuffer.wrap(it)
            while (localBuff.position() < localBuff.limit()) {
                inputChannel.readAvailable(localBuff)
            }
        }

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
                logger.info { "Received $typed" }
                packetChannel.send(typed)
            }
        }
    }

    suspend inline fun <reified T : IOnkyoResponse> send(p: IOnkyoResponse): T? {
        logger.info { "Sending $p" }
        return send(p.toPacket()) as? T
    }

    suspend fun send(p: Packet): IOnkyoResponse {
        val buff = ByteBuffer.wrap(p.bytes())
        outputChannel.writeFully(buff)
        return receive()
    }

    private suspend fun receive() = packetChannel.receive().also {
        logger.info { "Handling $it" }
    }

    override fun close() {
        conn.close()
    }

    companion object : KLogging()
}
