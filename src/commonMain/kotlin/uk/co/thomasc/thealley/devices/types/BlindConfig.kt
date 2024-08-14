package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.zigbee.blind.BlindState

@Serializable
@SerialName("Blind")
data class BlindConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig<BlindState>,
    IZigbeeConfig<BlindState>,
    IAlleyLightConfig,
    IConfigEditable<BlindConfig> by SimpleConfigEditable(
        listOf(
            BlindConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            BlindConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            BlindConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = BlindState()
    override val stateSerializer = BlindState.serializer()
}
