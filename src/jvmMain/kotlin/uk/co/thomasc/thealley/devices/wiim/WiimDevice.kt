package uk.co.thomasc.thealley.devices.wiim

import mu.KLogger
import mu.KLogging
import org.jupnp.DefaultUpnpServiceConfiguration
import org.jupnp.UpnpService
import org.jupnp.UpnpServiceImpl
import org.jupnp.model.meta.RemoteDevice
import org.jupnp.model.meta.Service
import org.jupnp.model.types.UDAServiceType
import org.jupnp.registry.DefaultRegistryListener
import org.jupnp.registry.Registry
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import uk.co.thomasc.thealley.devices.types.WiimConfig

class WiimDevice(id: Int, config: WiimConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<WiimDevice, WiimConfig, EmptyState>(id, config, state, stateStore) {

    val upnp = UpnpServiceImpl(DefaultUpnpServiceConfiguration())

    override suspend fun init(bus: AlleyEventBusShim) {
        upnp.startup()

        upnp.registry.addListener(object : DefaultRegistryListener() {
            override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
                val avTransport = device.findService(UDAServiceType("AVTransport")) ?: return
                logger.info { "Found renderer: ${device.details?.friendlyName} (${device.identity.udn})" }
                subscribe(bus, logger, upnp, avTransport)
            }

            override fun remoteDeviceRemoved(registry: Registry, device: RemoteDevice) =
                logger.warn { "Lost device: ${device.details?.friendlyName}" }
        })

        upnp.controlPoint.search()
    }

    private fun subscribe(bus: AlleyEventEmitter, log: KLogger, upnp: UpnpService, service: Service<*, *>) {
        val callback = WiimSubscription(bus, service)
        upnp.controlPoint.execute(callback)
    }

    override fun close() {
        upnp.shutdown()
    }

    companion object : KLogging()
}

data class WiimEvent(val uuid: String, val name: String, val state: TransportState) : IAlleyEvent
