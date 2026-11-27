package uk.co.thomasc.thealley.devices.wiim

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import mu.KLogging
import nl.adaptivity.xmlutil.XmlException
import nl.adaptivity.xmlutil.serialization.XML
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.types.WiimConfig

@Serializable
data class UpnpUpdate(
    @SerialName("LastChange")
    val lastChange: String
)

class WiimDevice(id: Int, config: WiimConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<WiimDevice, WiimConfig, EmptyState>(id, config, state, stateStore) {

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<MqttMessageEvent> { ev ->
            val parts = ev.topic.split('/')

            if (parts.size < 3 || parts[0] != "upnp" || parts[2] != "urn:upnp-org:serviceId:AVTransport") return@handle
            val uuid = parts[1].lowercase().removePrefix("uuid:")
            val type = parts[2]

            val msg = alleyJson.decodeFromString<UpnpUpdate>(ev.payload)

            try {
                val change = xml.decodeFromString<UpnpEvent>(msg.lastChange)
                val newState = change.transportState() ?: return@handle

                logger.info { "[$uuid] TransportState -> $newState" }

                bus.emit(WiimEvent(uuid, newState))
            } catch (e: XmlException) {
                logger.error(e) { "[$type] ${msg.lastChange}" }
            }
        }
    }

    companion object : KLogging() {
        val xml = XML.v1 { policy { ignoreUnknownChildren() } }
    }
}

data class WiimEvent(val uuid: String, val state: TransportState) : IAlleyEvent
