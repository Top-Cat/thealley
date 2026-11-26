package uk.co.thomasc.thealley.devices.wiim

import mu.KLogging
import org.jupnp.DefaultUpnpServiceConfiguration
import org.jupnp.UpnpServiceImpl
import org.jupnp.model.meta.RemoteDevice
import org.jupnp.model.types.UDAServiceType
import org.jupnp.protocol.ProtocolFactory
import org.jupnp.registry.DefaultRegistryListener
import org.jupnp.registry.Registry
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import uk.co.thomasc.thealley.devices.types.WiimConfig
import java.net.Inet4Address

val discoveryAddress = System.getenv("UPNP_IP")?.ifBlank { null }
val discoveryPort = System.getenv("UPNP_PORT")?.toIntOrNull() ?: 8081

class WiimDevice(id: Int, config: WiimConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<WiimDevice, WiimConfig, EmptyState>(id, config, state, stateStore) {

    val address = discoveryAddress?.let { Inet4Address.getByName(it) }

    val upnp = object : UpnpServiceImpl(DefaultUpnpServiceConfiguration(discoveryPort)) {
        override fun createRouter(protocolFactory: ProtocolFactory, registry: Registry?) =
            address?.let { FixedCallbackRouter(configuration, protocolFactory, address, discoveryPort) } ?: super.createRouter(protocolFactory, registry)
    }

    override suspend fun init(bus: AlleyEventBusShim) {
        upnp.startup()

        upnp.registry.addListener(object : DefaultRegistryListener() {
            override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
                val avTransport = device.findService(UDAServiceType("AVTransport")) ?: return
                logger.info { "Found renderer: ${device.details?.friendlyName} (${device.identity.udn})" }

                val callback = WiimSubscription(bus, avTransport)
                upnp.controlPoint.execute(callback)
            }

            override fun remoteDeviceRemoved(registry: Registry, device: RemoteDevice) =
                logger.warn { "Lost device: ${device.details?.friendlyName}" }
        })

        upnp.controlPoint.search()
    }

    override fun close() {
        upnp.shutdown()
    }

    companion object : KLogging()
}

data class WiimEvent(val uuid: String, val name: String, val state: TransportState) : IAlleyEvent
