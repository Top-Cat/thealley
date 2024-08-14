package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.somfy.SomfyBlindState

@Serializable
@SerialName("SomfyBlind")
data class SomfyBlindConfig(
    override val name: String,
    val deviceId: String,
    val prefix: String = "espsomfy"
) : IAlleyConfig<SomfyBlindState>,
    IAlleyLightConfig,
    IConfigEditable<SomfyBlindConfig> by SimpleConfigEditable(
        listOf(
            SomfyBlindConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            SomfyBlindConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            SomfyBlindConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = SomfyBlindState()
    override val stateSerializer = SomfyBlindState.serializer()
}
