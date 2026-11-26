package uk.co.thomasc.thealley.devices.wiim

import org.jupnp.UpnpServiceConfiguration
import org.jupnp.model.NetworkAddress
import org.jupnp.protocol.ProtocolFactory
import org.jupnp.transport.RouterImpl
import java.net.InetAddress

class FixedCallbackRouter(
    config: UpnpServiceConfiguration,
    protocolFactory: ProtocolFactory,
    private val callbackAddress: InetAddress,
    private val port: Int
) : RouterImpl(config, protocolFactory) {
    override fun getActiveStreamServers(preferredAddress: InetAddress?): List<NetworkAddress?> =
        listOf(
            NetworkAddress(callbackAddress, port, networkAddressFactory.getHardwareAddress(preferredAddress))
        )
}
