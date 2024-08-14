package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("Mqtt")
data class MqttConfig(
    override val name: String,
    val clientId: String,
    val host: String,
    val user: String,
    val pass: String
) : IAlleyConfig<EmptyState>,
    IConfigEditable<MqttConfig> by SimpleConfigEditable(
        listOf(
            MqttConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            MqttConfig::clientId.fieldEditor("Client ID") { c, n -> c.copy(clientId = n) },
            MqttConfig::host.fieldEditor("Host") { c, n -> c.copy(host = n) },
            MqttConfig::user.fieldEditor("Username") { c, n -> c.copy(user = n) },
            MqttConfig::pass.fieldEditor("Password", password = true) { c, n -> c.copy(pass = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
