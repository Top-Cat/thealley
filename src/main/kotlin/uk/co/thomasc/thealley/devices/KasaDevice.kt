package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.withTimeoutOrNull
import kotlinx.sockets.aSocket
import uk.co.thomasc.thealley.decryptWithHeader
import uk.co.thomasc.thealley.encryptWithHeader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer

abstract class KasaDevice<out T>(val host: String) {
    companion object {
        val mapper = jacksonObjectMapper()
        val threadPool = newFixedThreadPoolContext(10, "LocalClient")
    }

    protected abstract fun getData(): T?

    suspend fun getSysInfo(host: String, timeout: Long = 500): Any? =
        this.send("{\"system\":{\"get_sysinfo\":{}}}", host, timeout = timeout)?.let(::parseSysInfo)

    protected fun parseSysInfo(json: String): Any? {
        val node = try {
            mapper.readValue(json, JsonNode::class.java)
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
            "IOT.SMARTBULB" -> mapper.treeToValue(deviceNode, BulbData::class.java)
            "IOT.SMARTPLUGSWITCH" -> mapper.treeToValue(deviceNode, PlugData::class.java)
            else -> null
        }
    }

    suspend fun send(json: String, host: String, port: Int = 9999, timeout: Long) =
        withTimeoutOrNull(timeout) {
            async(threadPool) {
                try {
                    aSocket().tcp().connect(InetSocketAddress(InetAddress.getByName(host), port)).use {
                        val buff = ByteBuffer.wrap(encryptWithHeader(json))
                        it.write(buff)

                        val bb = ByteBuffer.allocate(8192)
                        it.read(bb)

                        // Packet says how long it should be
                        val len = ByteBuffer.wrap(bb.array().sliceArray(0..3)).int
                        while(bb.position() < len + 4) { it.read(bb) }

                        bb.flip()

                        String(decryptWithHeader(bb.array()))
                    }
                } catch (e: UnknownHostException) {
                    null
                } catch (e: SocketException) {
                    null
                }
            }.await()
        }
}
