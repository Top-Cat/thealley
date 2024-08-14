package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.somfy.SomfyGroupState

@Serializable
@SerialName("SomfyGroup")
data class SomfyGroupConfig(
    override val name: String,
    val deviceId: String,
    val prefix: String = "espsomfy"
) : IAlleyConfig<SomfyGroupState>,
    IAlleyRelayConfig,
    IConfigEditable<SomfyGroupConfig> by SimpleConfigEditable(
        listOf(
            SomfyGroupConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            SomfyGroupConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            SomfyGroupConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = SomfyGroupState()
    override val stateSerializer = SomfyGroupState.serializer()
}
