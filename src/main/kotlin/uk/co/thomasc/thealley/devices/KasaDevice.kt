package uk.co.thomasc.thealley.devices

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.decryptWithHeader
import uk.co.thomasc.thealley.encryptWithHeader
import java.lang.Exception
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer

abstract class KasaDevice<out T>(val host: String) {
    companion object : KLogging() {
        val threadPool = newFixedThreadPoolContext(10, "KasaDevice")
        val selector = ActorSelectorManager(Dispatchers.IO)
    }

    protected abstract suspend fun getData(): T?

    suspend fun getSysInfo(host: String, timeout: Long = 500): Any? =
        this.send("{\"system\":{\"get_sysinfo\":{}}}", host, timeout = timeout)?.let(::parseSysInfo)

    protected fun parseSysInfo(json: String): Any? {
        logger.debug { "Received json from kasa - $json" }
        val node = try {
            alleyJson.parseToJsonElement(json).jsonObject
        } catch (e: SerializationException) {
            logger.warn { "Failed to parse json from $host" }
            return null
        }
        val deviceNode = node["system"]?.jsonObject?.get("get_sysinfo")
        val deviceNodeObj = deviceNode?.jsonObject

        val type = when {
            deviceNodeObj?.containsKey("mic_type") == true -> deviceNodeObj["mic_type"]
            deviceNodeObj?.containsKey("type") == true -> deviceNodeObj["type"]
            else -> JsonPrimitive("")
        }?.jsonPrimitive?.content

        return when (type) {
            "IOT.SMARTBULB" -> alleyJson.decodeFromJsonElement<BulbData>(deviceNode!!)
            "IOT.SMARTPLUGSWITCH" -> alleyJson.decodeFromJsonElement<PlugData>(deviceNode!!)
            else -> null
        }
    }

    suspend fun send(json: String, host: String, port: Int = 9999, timeout: Long) =
        withTimeoutOrNull(timeout) {
            async(threadPool) {
                try {
                    aSocket(selector).tcp().connect(host, port).use {
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
