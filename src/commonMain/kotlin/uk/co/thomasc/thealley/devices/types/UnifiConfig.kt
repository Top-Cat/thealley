package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("Unifi")
data class UnifiConfig(
    override val name: String,
    val mainNetwork: String,
    val guestNetwork: String,
    val guestPassword: String
) : IAlleyConfig<EmptyState>,
    IConfigEditable<UnifiConfig> by SimpleConfigEditable(
        listOf(
            UnifiConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            UnifiConfig::mainNetwork.fieldEditor("Main Network") { c, n -> c.copy(mainNetwork = n) },
            UnifiConfig::guestNetwork.fieldEditor("Guest Network") { c, n -> c.copy(guestNetwork = n) },
            UnifiConfig::guestPassword.fieldEditor("Guest Password", password = true) { c, n -> c.copy(guestPassword = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
