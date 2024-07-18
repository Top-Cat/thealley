package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor

@Serializable
@SerialName("Onkyo")
data class OnkyoConfig(
    override val name: String,
    val host: String
) : IAlleyConfig,
    IAlleyRelayConfig,
    IConfigEditable<OnkyoConfig> by SimpleConfigEditable(
        listOf(
            OnkyoConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            OnkyoConfig::host.fieldEditor("Host") { c, n -> c.copy(host = n) }
        )
    )
