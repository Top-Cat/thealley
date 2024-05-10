package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("netservicelist")
data class NetServiceList(override val count: Int, @XmlElement(true) override val list: List<NetService>) : OnkyoList<NetServiceList.NetService> {
    @Serializable
    @SerialName("netservice")
    data class NetService(
        val id: String,
        val value: Boolean,
        val name: String,
        val account: String? = null,
        val password: String? = null,
        val zone: String,
        val enable: String,
        @SerialName("addqueue") val addQueue: Boolean? = null,
        val sort: Boolean? = null,
        val multipage: Boolean?
    )
}
