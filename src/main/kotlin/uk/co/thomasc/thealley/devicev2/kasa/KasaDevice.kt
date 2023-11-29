package uk.co.thomasc.thealley.devicev2.kasa

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.client.alleyJsonUgly
import uk.co.thomasc.thealley.decryptWithHeader
import uk.co.thomasc.thealley.devices.BulbData
import uk.co.thomasc.thealley.devices.PlugData
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.types.IKasaConfig
import uk.co.thomasc.thealley.encryptWithHeader
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer
import kotlin.time.Duration.Companion.seconds

abstract class KasaDevice<X, T : AlleyDevice<T, U, V>, U : IKasaConfig, V : IKasaState>(id: Int, config: U, state: V, stateStore: IStateUpdater<V>) :
    AlleyDevice<T, U, V>(id, config, state, stateStore) {

    protected var deviceData: X? = null
    private var lastRequest = Instant.DISTANT_PAST
    private val mutex = Mutex()

    protected suspend fun <Y> makeRequest(block: suspend () -> Y) = mutex.withLock {
        block().also {
            lastRequest = Clock.System.now()
        }
    }

    protected suspend fun getData() = mutex.withLock {
        updateData()
    }

    private suspend fun updateData() = run {
        if (Clock.System.now().minus(20.seconds) > lastRequest) {
            (getSysInfo(3000) as? X)?.apply {
                deviceData = this
            }
        }

        lastRequest = Clock.System.now()
        deviceData
    }

    protected suspend inline fun <reified T> send(obj: T) = send(alleyJsonUgly.encodeToString(obj), timeout = 500)
    protected suspend fun send(json: String) = send(json, timeout = 500)

    private suspend fun getSysInfo(timeout: Long = 500): Any? =
        this.send("{\"system\":{\"get_sysinfo\":{}}}", timeout = timeout)?.let(::parseSysInfo)

    protected fun parseSysInfo(json: String): Any? {
        logger.debug { "Received json from kasa - $json" }
        val node = try {
            alleyJson.parseToJsonElement(json).jsonObject
        } catch (e: SerializationException) {
            logger.warn { "Failed to parse json from ${config.host}" }
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

    protected suspend fun send(json: String, port: Int = 9999, timeout: Long) =
        withTimeoutOrNull(timeout) {
            async(threadPool) {
                try {
                    aSocket(selector).tcp().connect(config.host, port).use {
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

    companion object : KLogging() {
        val threadPool = newFixedThreadPoolContext(10, "KasaDevice")
        val selector = ActorSelectorManager(Dispatchers.IO)
    }
}
