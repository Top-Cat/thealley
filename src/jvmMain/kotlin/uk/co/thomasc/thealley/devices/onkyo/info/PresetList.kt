package uk.co.thomasc.thealley.devices.onkyo.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("presetlist")
data class PresetList(override val count: Int, @XmlElement(true) override val list: List<Preset>) : OnkyoList<PresetList.Preset> {
    @Serializable
    @SerialName("preset")
    data class Preset(
        val id: String,
        val band: Int,
        @SerialName("freq") val frequency: Float,
        val name: String
    )
}
