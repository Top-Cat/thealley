package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.kasa.plug.PlugState

@Serializable
@SerialName("Plug")
data class PlugConfig(
    override val name: String,
    override val host: String
) : IAlleyConfig<PlugState>,
    IKasaConfig<PlugState>,
    IAlleyRelayConfig,
    IConfigEditable<PlugConfig> by SimpleConfigEditable(
        listOf(
            PlugConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            PlugConfig::host.fieldEditor("Host") { c, n -> c.copy(host = n) }
        )
    ) {
    override val defaultState = PlugState(false)
    override val stateSerializer = PlugState.serializer()
}
