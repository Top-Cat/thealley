package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("selectorlist")
data class SelectorList(override val count: Int, @XmlElement(true) override val list: List<Selector>) : OnkyoList<SelectorList.Selector> {
    @Serializable
    @SerialName("selector")
    data class Selector(
        val id: String,
        val value: Int,
        val name: String,
        val zone: String,
        @SerialName("iconid") val iconId: String? = null
    ) {
        fun isEnabled() = value != 0
    }
}
