package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("device")
data class Device(
    val id: String,
    @XmlElement(true) val brand: String,
    @XmlElement(true) val category: String,
    @XmlElement(true) val year: Int,
    @XmlElement(true) val model: String,
    @XmlElement(true) val destination: String,
    @XmlElement(true) @SerialName("macaddress") val macAddress: String,
    @XmlElement(true) @SerialName("modeliconurl") val modelIconUrl: String,
    @XmlElement(true) @SerialName("friendlyname") val friendlyName: String,
    @XmlElement(true) @SerialName("firmwareversion") val firmwareVersion: String,
    @XmlElement(true) @SerialName("ecosystemversion") val ecoSystemVersion: String,
    @XmlElement(true) val netServiceList: NetServiceList,
    @XmlElement(true) val zoneList: ZoneList,
    @XmlElement(true) val selectorList: SelectorList,
    @XmlElement(true) val presetList: PresetList,
    @XmlElement(true) val controlList: ControlList,
    @XmlElement(true) val function: FunctionList,
    @XmlElement(true) val tuners: Tuners
)
