package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor

@Serializable
@SerialName("DualDimmer")
data class DualDimmerConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig,
    IZigbeeConfig,
    IAlleyDualLightConfig,
    IConfigEditable<DualDimmerConfig> by SimpleConfigEditable(
        listOf(
            DualDimmerConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            DualDimmerConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            DualDimmerConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    )
