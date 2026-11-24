package uk.co.thomasc.thealley.devices.wiim

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Event", "urn:schemas-upnp-org:metadata-1-0/AVT/", "")
data class UpnpEvent(
    @XmlSerialName("InstanceID")
    val instance: InstanceId
) {
    fun transportState() = instance.transportState?.value
}

@Serializable
data class InstanceId(
    @XmlSerialName("TransportState")
    val transportState: ValueElement<TransportState>? = null
)

@Serializable
data class ValueElement<T>(
    @SerialName("val")
    @XmlElement(false)
    val value: T
)

enum class TransportState {
    STOPPED,
    PLAYING,
    PAUSED_PLAYBACK,
    TRANSITIONING,
    NO_MEDIA_PRESENT,
    PAUSED_RECORDING,
    RECORDING
}
