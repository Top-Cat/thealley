package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor

@Serializable
@SerialName("DualSwitch")
data class DualSwitchConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig,
    IZigbeeConfig,
    IAlleyDualRelayConfig,
    IConfigEditable<DualSwitchConfig> by SimpleConfigEditable(
        listOf(
            DualSwitchConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            DualSwitchConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            DualSwitchConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    )
