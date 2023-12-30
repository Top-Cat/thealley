package uk.co.thomasc.thealley.devices.onkyo

import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.network.sockets.toJavaAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.DatagramPacket
import java.net.DatagramSocket

val magic = Packet("!xECNQSTN")

data class DiscoveryResult(
    val address: InetSocketAddress,
    val deviceCategory: Int,
    val model: String,
    val iscpPort: Int,
    val areaCode: String,
    val identifier: String
)

suspend fun receiveDiscovery(socket: DatagramSocket): List<DiscoveryResult?> {
    val buffer = ByteArray(1024)
    val receivedPacket = DatagramPacket(buffer, buffer.size)
    val result = try {
        withContext(Dispatchers.IO) {
            socket.receive(receivedPacket)
        }

        val regex = Regex("!(\\d)ECN([^/]*)/(\\d{5})/(\\w{2})/(.{0,12})")
        val packet = Packet.parse(buffer).content()
        regex.matchAt(packet, 0)
    } catch (e: SocketTimeoutException) {
        return listOf()
    }

    return receiveDiscovery(socket) + if (result != null) {
        val (_, deviceCategory, modelName, iscpPort, areaCode, identifier) = result.groupValues
        DiscoveryResult(InetSocketAddress(receivedPacket.address.hostName, receivedPacket.port), deviceCategory.toInt(), modelName, iscpPort.toInt(), areaCode, identifier)
    } else {
        null
    }
}

suspend fun discovery(timeout: Long) =
    withTimeoutOrNull(timeout) {
        val address = InetSocketAddress("10.48.3.255", 60128)
        val socket = DatagramSocket()
        socket.soTimeout = 100
        socket.broadcast = true

        val bytes = magic.bytes()
        socket.send(DatagramPacket(bytes, bytes.size, address.toJavaAddress()))

        receiveDiscovery(socket)
    }

private operator fun <E> List<E>.component6() = this[5]
