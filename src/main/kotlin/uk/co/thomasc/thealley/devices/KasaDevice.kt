package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withTimeoutOrNull
import uk.co.thomasc.thealley.client.jackson
import uk.co.thomasc.thealley.decryptWithHeader
import uk.co.thomasc.thealley.encryptWithHeader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer

abstract class KasaDevice<out T>(val host: String) {
    companion object {
        val threadPool = newFixedThreadPoolContext(10, "KasaDevice")
        val selector = ActorSelectorManager(Dispatchers.IO)
    }

    protected abstract fun getData(): T?

    suspend fun getSysInfo(host: String, timeout: Long = 500): Any? =
        this.send("{\"system\":{\"get_sysinfo\":{}}}", host, timeout = timeout)?.let(::parseSysInfo)

    protected fun parseSysInfo(json: String): Any? {
        val node = try {
            jackson.readValue(json, JsonNode::class.java)
        } catch (e: JsonParseException) {
            println("Failed to parse json from $host")
            return null
        }
        val deviceNode = node.get("system").get("get_sysinfo")

        val type = when {
            deviceNode.has("mic_type") -> deviceNode.get("mic_type").textValue()
            deviceNode.has("type") -> deviceNode.get("type").textValue()
            else -> ""
        }

        return when (type) {
            "IOT.SMARTBULB" -> jackson.treeToValue(deviceNode, BulbData::class.java)
            "IOT.SMARTPLUGSWITCH" -> jackson.treeToValue(deviceNode, PlugData::class.java)
            else -> null
        }
    }

    suspend fun send(json: String, host: String, port: Int = 9999, timeout: Long) =
        withTimeoutOrNull(timeout) {
            async(threadPool) {
                try {
                    aSocket(selector).tcp().connect(InetSocketAddress(InetAddress.getByName(host), port)).use {
                        val buff = ByteBuffer.wrap(encryptWithHeader(json))
                        val inputChannel = it.openReadChannel()
                        val outputChannel = it.openWriteChannel(true)
                        outputChannel.writeFully(buff)

                        val bb = ByteBuffer.allocate(8192)
                        val start = System.currentTimeMillis()
                        inputChannel.readAvailable(bb)

                        // Packet says how long it should be
                        val len = ByteBuffer.wrap(bb.array().sliceArray(0..3)).int
                        while (bb.position() < len + 4) {
                            if (System.currentTimeMillis() - start > timeout) {
                                return@use null
                            }
                            inputChannel.readAvailable(bb)
                        }

                        bb.flip()

                        String(decryptWithHeader(bb.array()))
                    }
                } catch (e: UnknownHostException) {
                    null
                } catch (e: SocketException) {
                    e.printStackTrace()
                    null
                }
            }.await()
        }
}
