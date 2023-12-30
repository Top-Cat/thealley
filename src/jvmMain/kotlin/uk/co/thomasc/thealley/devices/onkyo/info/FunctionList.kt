package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("functionlist")
data class FunctionList(override val count: Int, @XmlElement(true) override val list: List<Function>) : OnkyoList<FunctionList.Function> {
    @Serializable
    @SerialName("function")
    data class Function(
        val id: String,
        val value: Boolean
    )
}
