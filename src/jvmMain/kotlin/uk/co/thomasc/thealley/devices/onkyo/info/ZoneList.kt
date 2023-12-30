package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("zonelist")
data class ZoneList(override val count: Int, @XmlElement(true) override val list: List<Zone>) : OnkyoList<ZoneList.Zone> {
    @Serializable
    @SerialName("zone")
    data class Zone(
        val id: Int,
        val value: Boolean,
        val name: String,
        @SerialName("volmax") val volumeMax: Int,
        @SerialName("volstep") val volumeStep: Int,
        @SerialName("src") val source: Int,
        @SerialName("dst") val destination: Int,
        @SerialName("lrselect") val leftRightSelect: Int
    )
}
