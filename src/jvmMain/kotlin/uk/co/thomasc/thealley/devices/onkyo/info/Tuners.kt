package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("tuners")
data class Tuners(override val count: Int, @XmlElement(true) override val list: List<Tuner>) : OnkyoList<Tuners.Tuner> {
    @Serializable
    @SerialName("tuner")
    data class Tuner(
        val band: String,
        val min: Int,
        val max: Int,
        val step: Int
    )
}
