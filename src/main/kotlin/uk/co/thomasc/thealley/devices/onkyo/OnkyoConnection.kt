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
import uk.co.thomasc.thealley.devices.kasa.KasaDevice
import uk.co.thomasc.thealley.devices.onkyo.packet.IOnkyoResponse
import java.nio.ByteBuffer
import java.nio.charset.Charset

class OnkyoConnection(private val host: String, private val port: Int = 60128) : Closeable {
    private lateinit var conn: Socket
    private lateinit var inputChannel: ByteReadChannel
    private lateinit var outputChannel: ByteWriteChannel
    private val packetChannel = Channel<Packet>(10)

    suspend fun init() {
        // I like big buffers and I cannot lie. Can't work out how to make this non-global
        System.setProperty("io.ktor.utils.io.BufferSize", "10240")
        conn = aSocket(KasaDevice.selector).tcp().connect(host, port).apply {
            inputChannel = openReadChannel()
            outputChannel = openWriteChannel(true)
        }

        GlobalScope.launch {
            receiveLoop()
        }
    }

    private suspend fun receiveLoop() {
        while (!conn.isClosed) {
            inputChannel.read(4) { localBuff ->
                val magic = ByteArray(4)
                localBuff.get(magic)

                magic.toString(Charset.defaultCharset()) == "ISCP" || throw OnkyoParseException("Bad magic")
            }

            val headerLength = inputChannel.readInt()
            val contentLength = inputChannel.readInt()

            val version = inputChannel.readByte()
            version == 1.toByte() || throw OnkyoParseException("Bad version")

            val remainingHeader = headerLength - 13
            inputChannel.read(remainingHeader) { localBuff ->
                localBuff.position(localBuff.position() + remainingHeader)
            }

            val content = ByteArray(contentLength)
            inputChannel.read(contentLength) { localBuff ->
                localBuff.get(content)
            }

            packetChannel.send(Packet(content))
        }
    }

    suspend fun <T : IOnkyoResponse> send(p: IOnkyoResponse): T? = send(p.toPacket()).typed() as? T

    suspend fun send(p: Packet): Packet {
        val buff = ByteBuffer.wrap(p.bytes())
        outputChannel.writeFully(buff)
        return receive()
    }

    suspend fun receive() = packetChannel.receive()

    override fun close() {
        conn.close()
    }
}
