package uk.co.thomasc.thealley

import java.net.Inet4Address
import java.net.NetworkInterface
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class LocalDiscovery {
    fun start() {
        val lanIp = NetworkInterface.getByName("net1")
            .inetAddresses.asSequence()
            .filterIsInstance<Inet4Address>()
            .first()

        val jmdns = JmDNS.create(lanIp)
        val discoveryData = mapOf(
            "id" to "thealley",
            "ip" to "10.2.238.149",
            "port" to "8080"
        )
        val serviceInfo = ServiceInfo.create("_alley._tcp.local.", "hub", 8080, 1, 1, false, discoveryData)
        jmdns.registerService(serviceInfo)
    }
}
