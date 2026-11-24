package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("WiiM")
data class WiimConfig(
    override val name: String
) : IAlleyConfig<EmptyState>,
    IAlleyRelayConfig,
    IConfigEditable<WiimConfig> by SimpleConfigEditable(
        listOf(
            WiimConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
