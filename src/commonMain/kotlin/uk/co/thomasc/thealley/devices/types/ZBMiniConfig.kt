package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("ZBMini")
data class ZBMiniConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig<EmptyState>,
    IZigbeeConfig<EmptyState>,
    IAlleyRelayConfig,
    IConfigEditable<ZBMiniConfig> by SimpleConfigEditable(
        listOf(
            ZBMiniConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            ZBMiniConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            ZBMiniConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
