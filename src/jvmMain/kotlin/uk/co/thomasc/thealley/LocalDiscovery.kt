package uk.co.thomasc.thealley

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.Serializable
import mu.KLogging

class LocalDiscovery {
    fun start() {
        val discoverServer = aSocket(selector).udp().bind(InetSocketAddress("0.0.0.0", 5555))

        CoroutineScope(threadPool).launch {
            while (true) {
                val packet = discoverServer.receive()
                val message = packet.packet.readBytes().decodeToString()
                println("Received from ${packet.address} -> $message")

                val response = "thealley".encodeToByteArray()
                discoverServer.send(Datagram(ByteReadPacket(response), packet.address))
            }
        }
    }

    companion object : KLogging() {
        val threadPool = newFixedThreadPoolContext(2, "LocalDiscovery")
        val selector = ActorSelectorManager(Dispatchers.IO)
    }
}
