package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.EmptyState

@Serializable
@SerialName("ZPlug")
data class ZPlugConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig<EmptyState>,
    IZigbeeConfig<EmptyState>,
    IAlleyRelayConfig,
    IConfigEditable<ZPlugConfig> by SimpleConfigEditable(
        listOf(
            ZPlugConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            ZPlugConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            ZPlugConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = EmptyState
    override val stateSerializer = EmptyState.serializer()
}
