package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
data class ReceiverInformation(val status: String, @XmlElement(true) val device: Device)
