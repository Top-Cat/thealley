package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("SwitchServer")
data class SwitchServerConfig(
    override val name: String,
    val port: Int
) : IAlleyConfig<EmptyState>,
    IConfigEditable<SwitchServerConfig> by SimpleConfigEditable(
        listOf(
            SwitchServerConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            SwitchServerConfig::port.fieldEditor("Port") { c, n -> c.copy(port = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
