package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.ps2.PS2State

@Serializable
@SerialName("PS2")
data class PS2Config(
    override val name: String,
    val prefix: String = "ps2"
) : IAlleyConfig<PS2State>,
    IAlleyRelayConfig,
    IConfigEditable<PS2Config> by SimpleConfigEditable(
        listOf(
            PS2Config::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            PS2Config::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = PS2State()
    override val stateSerializer = PS2State.serializer()
}
