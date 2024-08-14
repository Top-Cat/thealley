package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("Onkyo")
data class OnkyoConfig(
    override val name: String,
    val host: String
) : IAlleyConfig<EmptyState>,
    IAlleyRelayConfig,
    IConfigEditable<OnkyoConfig> by SimpleConfigEditable(
        listOf(
            OnkyoConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            OnkyoConfig::host.fieldEditor("Host") { c, n -> c.copy(host = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
