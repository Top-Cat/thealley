package uk.co.thomasc.thealley

import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class LocalDiscovery {
    fun start() {
        val jmdns = JmDNS.create(InetAddress.getByName("10.2.1.2"))
        val discoveryData = mapOf(
            "id" to "thealley",
            "ip" to InetAddress.getLocalHost().hostAddress,
            "port" to "8080"
        )
        val serviceInfo = ServiceInfo.create("_alley._tcp.local.", "hub", 8080, 1, 1, false, discoveryData)
        jmdns.registerService(serviceInfo)
    }
}
