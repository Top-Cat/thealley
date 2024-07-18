package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor

@Serializable
@SerialName("Motion")
data class MotionConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig,
    IZigbeeConfig,
    IConfigEditable<MotionConfig> by SimpleConfigEditable(
        listOf(
            MotionConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            MotionConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            MotionConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    )
