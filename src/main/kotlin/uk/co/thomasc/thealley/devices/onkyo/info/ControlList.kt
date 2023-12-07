package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("controllist")
data class ControlList(override val count: Int, @XmlElement(true) override val list: List<Control>) : OnkyoList<ControlList.Control> {
    @Serializable
    @SerialName("control")
    data class Control(
        val id: String,
        val value: Boolean,
        val zone: Int? = null,
        val min: Int? = null,
        val max: Int? = null,
        val step: Int? = null,
        val code: String? = null,
        val position: Int? = null
    )
}
