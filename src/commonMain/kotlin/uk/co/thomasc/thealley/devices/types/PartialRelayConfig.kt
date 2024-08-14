package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("PartialRelay")
data class PartialRelayConfig(
    override val name: String,
    val device: Int,
    val index: Int
) : IAlleyConfig<EmptyState>,
    IConfigEditable<PartialRelayConfig> by SimpleConfigEditable(
        listOf(
            PartialRelayConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            PartialRelayConfig::device.fieldEditor("Device", { it is IAlleyDualRelayConfig }) { c, n -> c.copy(device = n) },
            PartialRelayConfig::index.fieldEditor("Index") { c, n -> c.copy(device = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
