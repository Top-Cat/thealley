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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import mu.KLogging
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.client.alleyJsonUgly
import uk.co.thomasc.thealley.decryptWithHeader
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.kasa.plug.data.KasaData
import uk.co.thomasc.thealley.devicev2.kasa.plug.data.KasaResponse
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

    protected suspend inline fun <reified T : KasaResponse<U>, U : KasaData> getData() = getData(serializer<T>())

    protected suspend fun <T : KasaResponse<U>, U> getData(serializer: KSerializer<T>) = mutex.withLock {
        if (Clock.System.now().minus(20.seconds) > lastRequest) {
            (getSysInfo(serializer, 3000) as? X)?.apply {
                deviceData = this
            }
        }

        lastRequest = Clock.System.now()
        deviceData
    }

    protected suspend inline fun <reified T> send(obj: T) = send(alleyJsonUgly.encodeToString(obj), timeout = 500)
    protected suspend fun send(json: String) = send(json, timeout = 500)

    private suspend inline fun <reified T : KasaResponse<U>, U> getSysInfo(timeout: Long = 500): T? =
        getSysInfo(serializer<T>(), timeout)

    private suspend fun <T : KasaResponse<U>, U> getSysInfo(serializer: KSerializer<T>, timeout: Long = 500): T? =
        this.send("{\"system\":{\"get_sysinfo\":{}}}", timeout = timeout)?.let {
            parseSysInfo(serializer, it)
        }

    protected inline fun <reified T : KasaResponse<U>, U> parseSysInfo(json: String) = parseSysInfo(serializer<T>(), json)

    protected fun <T : KasaResponse<*>> parseSysInfo(serializer: KSerializer<T>, json: String): T? {
        logger.info { "Received json from kasa - $json" }

        return try {
            alleyJson.decodeFromString(serializer, json)
        } catch (e: SerializationException) {
            logger.warn { "Failed to parse json from ${config.host}" }
            return null
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
        val threadPool = newFixedThreadPoolContext(4, "KasaDevice")
        val selector = ActorSelectorManager(Dispatchers.IO)
    }
}
