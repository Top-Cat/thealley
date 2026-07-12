package uk.co.thomasc.thealley

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

val discoveryInterface = System.getenv("DISCOVERY_IFACE") ?: "net1"

class LocalDiscovery {
    fun start() {
        val address = NetworkInterface.getByName(discoveryInterface)
            .inetAddresses.asSequence()
            .filterIsInstance<Inet4Address>()
            .first()

        val jmdns = JmDNS.create(address)
        val discoveryData = mapOf(
            "id" to "thealley",
            "ip" to InetAddress.getLocalHost().hostAddress,
            "port" to "8080"
        )
        val serviceInfo = ServiceInfo.create("_alley._tcp.local.", "hub", 8080, 1, 1, false, discoveryData)
        jmdns.registerService(serviceInfo)
    }
}
