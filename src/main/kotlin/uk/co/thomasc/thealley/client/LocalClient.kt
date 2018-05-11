package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.sockets.aSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.withTimeoutOrNull
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.decryptWithHeader
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.BulbData
import uk.co.thomasc.thealley.devices.Plug
import uk.co.thomasc.thealley.devices.PlugData
import uk.co.thomasc.thealley.encryptWithHeader
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer

val mapper = jacksonObjectMapper()

interface DeviceResponse {
    fun <T> bulb(l: (Bulb?) -> T): Deferred<T?>
    fun <T> plug(l: (Plug?) -> T): Deferred<T?>
}

@Component
class LocalClient {

    val threadPool = newFixedThreadPoolContext(10, "LocalClient")

    fun getDevice(host: String, timeout: Long = 2000): DeviceResponse {
        val deviceInfo = async(threadPool) {
            getSysInfo(host, timeout)
        }

        return object : DeviceResponse {
            override fun <T> bulb(l: (Bulb?) -> T): Deferred<T?> =
                async(CommonPool) {
                    deviceInfo.await().let {
                        when (it) {
                            is BulbData? -> it.let{
                                bulbData -> l(bulbData?.let { b: BulbData -> Bulb(this@LocalClient, host, b) })
                            }
                            else -> null
                        }
                    }
                }

            override fun <T> plug(l: (Plug?) -> T): Deferred<T?> =
                async(CommonPool) {
                    deviceInfo.await().let {
                        when (it) {
                            is PlugData? -> it.let{
                                plugData -> l(plugData?.let { b: PlugData -> Plug(this@LocalClient, host, b) })
                            }
                            else -> null
                        }
                    }
                }
        }
    }

    suspend fun getSysInfo(host: String, timeout: Long = 500): Any? =
        this.send("{\"system\":{\"get_sysinfo\":{}}}", host, timeout = timeout)?.let { json ->

            val node = try {
                mapper.readValue(json, JsonNode::class.java)
            } catch (e: JsonParseException) {
                println("Failed to parse json from $host")
                return@let null
            }
            val deviceNode = node.get("system").get("get_sysinfo")

            val type = if (deviceNode.has("mic_type")) {
                deviceNode.get("mic_type").textValue()
            } else if (deviceNode.has("type")) {
                deviceNode.get("type").textValue()
            } else {
                ""
            }

            when (type) {
                "IOT.SMARTBULB" -> mapper.treeToValue(deviceNode, BulbData::class.java)
                "IOT.SMARTPLUGSWITCH" -> mapper.treeToValue(deviceNode, PlugData::class.java)
                else -> null
            }
        }

    suspend fun send(json: String, host: String, port: Int = 9999, timeout: Long) =
        withTimeoutOrNull(timeout) {
            async(threadPool) {
                try {
                    aSocket().tcp().connect(InetSocketAddress(InetAddress.getByName(host), 9999)).use {
                        val buff = ByteBuffer.wrap(encryptWithHeader(json))
                        it.write(buff)

                        val bb = ByteBuffer.allocate(8192)
                        it.read(bb)
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
