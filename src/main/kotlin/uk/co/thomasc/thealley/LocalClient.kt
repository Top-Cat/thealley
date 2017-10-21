package uk.co.thomasc.thealley

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.sockets.aSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.BulbData
import uk.co.thomasc.thealley.devices.Plug
import uk.co.thomasc.thealley.devices.PlugData
import java.nio.ByteBuffer

val mapper = jacksonObjectMapper()

interface DeviceResponse {
    fun <T> then(l: (Any) -> T): Deferred<T?>
}

@Component
class LocalClient {

    fun getDevice(host: String): DeviceResponse {
        val deviceInfo = async(CommonPool) {
            getSysInfo(host)
        }

        return object : DeviceResponse {
            override fun <T> then(l: (Any) -> T): Deferred<T?> =
                async(CommonPool) {
                    deviceInfo.await()?.let {
                        when (it) {
                            is BulbData -> l(Bulb(this@LocalClient, host, it))
                            is PlugData -> l(Plug(this@LocalClient, host, it))
                            else -> null
                        }
                    }
                }
        }
    }

    suspend fun getSysInfo(host: String): Any? {
        val json = this.send("{\"system\":{\"get_sysinfo\":{}}}", host)

        val node = mapper.readValue(json, JsonNode::class.java)
        val deviceNode = node.get("system").get("get_sysinfo")

        val type = if (deviceNode.has("mic_type")) {
            deviceNode.get("mic_type").textValue()
        } else if (deviceNode.has("type")) {
            deviceNode.get("type").textValue()
        } else {
            ""
        }

        return when (type) {
            "IOT.SMARTBULB" -> mapper.treeToValue(deviceNode, BulbData::class.java)
            "IOT.SMARTPLUGSWITCH" -> mapper.treeToValue(deviceNode, PlugData::class.java)
            else -> null
        }
    }

    suspend fun send(json: String, host: String, port: Int = 9999) =
        aSocket().tcp().connect(InetSocketAddress(InetAddress.getByName(host), 9999)).use {
            val buff = ByteBuffer.wrap(encryptWithHeader(json))
            it.write(buff)

            val bb = ByteBuffer.allocate(8192)
            it.read(bb)
            bb.flip()

            String(decryptWithHeader(bb.array()))
        }

}
