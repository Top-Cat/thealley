package uk.co.thomasc.thealley

import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class LocalDiscovery {
    fun start() {
        val jmdns = JmDNS.create()
        val serviceInfo = ServiceInfo.create("_alley._tcp.local.", "hub", 8080, "Home automation")
        jmdns.registerService(serviceInfo)
    }
}
