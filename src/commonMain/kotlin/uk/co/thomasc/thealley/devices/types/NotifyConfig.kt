package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("Notify")
data class NotifyConfig(
    override val name: String,
    val session: String = "default",
    val baseUrl: String = "http://waha",
    val users: List<String> = listOf()
) : IAlleyConfig<EmptyState>,
    IConfigEditable<NotifyConfig> by SimpleConfigEditable(
        listOf(
            NotifyConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            NotifyConfig::session.fieldEditor("Session") { c, n -> c.copy(session = n) },
            NotifyConfig::baseUrl.fieldEditor("Base URL") { c, n -> c.copy(baseUrl = n) },
            NotifyConfig::users.fieldEditor("Users") { c, n -> c.copy(users = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
