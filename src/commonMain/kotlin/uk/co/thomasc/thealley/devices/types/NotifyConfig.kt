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
    val topic: String = "default",
    val baseUrl: String = "http://ntfy.web",
    val token: String = ""
) : IAlleyConfig<EmptyState>,
    IConfigEditable<NotifyConfig> by SimpleConfigEditable(
        listOf(
            NotifyConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            NotifyConfig::topic.fieldEditor("Topic") { c, n -> c.copy(topic = n) },
            NotifyConfig::baseUrl.fieldEditor("Base URL") { c, n -> c.copy(baseUrl = n) },
            NotifyConfig::token.fieldEditor("Token") { c, n -> c.copy(token = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
