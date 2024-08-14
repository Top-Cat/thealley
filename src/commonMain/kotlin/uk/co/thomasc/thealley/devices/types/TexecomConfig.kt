package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.alarm.TexecomState

@Serializable
@SerialName("Texecom")
data class TexecomConfig(
    override val name: String,
    val prefix: String = "texecom2mqtt"
) : IAlleyConfig<TexecomState>,
    IConfigEditable<TexecomConfig> by SimpleConfigEditable(
        listOf(
            TexecomConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            TexecomConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = TexecomState()
    override val stateSerializer = TexecomState.serializer()
}
