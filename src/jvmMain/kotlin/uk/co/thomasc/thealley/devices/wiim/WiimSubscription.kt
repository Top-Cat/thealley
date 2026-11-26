package uk.co.thomasc.thealley.devices.wiim

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.decodeFromString
import mu.KLogging
import nl.adaptivity.xmlutil.XmlException
import nl.adaptivity.xmlutil.serialization.XML
import org.jupnp.controlpoint.ControlPoint
import org.jupnp.controlpoint.SubscriptionCallback
import org.jupnp.model.gena.CancelReason
import org.jupnp.model.gena.GENASubscription
import org.jupnp.model.message.UpnpResponse
import org.jupnp.model.meta.Service
import uk.co.thomasc.thealley.devices.AlleyEventEmitter

class WiimSubscription(val bus: AlleyEventEmitter, service: Service<*, *>, controlPoint: ControlPoint) : SubscriptionCallback(service, 600) {
    private val device = service.device.details.friendlyName
    private val udn = service.device.identity.udn.identifierString

    init {
        controlPoint.execute(this)
    }

    override fun established(sub: GENASubscription<*>) {
        log.debug { "Subscribed: ${sub.subscriptionId}" }
    }

    override fun eventReceived(sub: GENASubscription<*>) {
        // AVTransport sends its state as one XML blob in the "LastChange" variable
        val lastChange = sub.currentValues["LastChange"]?.value?.toString() ?: return
        try {
            val change = xml.decodeFromString<UpnpEvent>(lastChange)
            val newState = change.transportState() ?: return

            log.info { "[$device] TransportState -> $newState" }

            scope.launch {
                bus.emit(WiimEvent(udn, device, newState))
            }
        } catch (e: XmlException) {
            log.error(e) { lastChange }
        }
    }

    override fun eventsMissed(sub: GENASubscription<*>, numberOfMissedEvents: Int) =
        log.warn { "Missed $numberOfMissedEvents events" }

    override fun ended(sub: GENASubscription<*>, reason: CancelReason?, response: UpnpResponse?) =
        log.warn { "Subscription ended: $reason" }

    override fun failed(sub: GENASubscription<*>?, response: UpnpResponse?, ex: Exception?, defaultMsg: String?) {
        log.error { "Subscription failed: $defaultMsg" }
        controlPoint.execute(this)
    }

    companion object : KLogging() {
        private val threadPool = newFixedThreadPoolContext(2, "WiimDevice")
        val scope = CoroutineScope(threadPool)

        val log = logger
        val xml = XML.v1 { policy { ignoreUnknownChildren() } }
    }
}
