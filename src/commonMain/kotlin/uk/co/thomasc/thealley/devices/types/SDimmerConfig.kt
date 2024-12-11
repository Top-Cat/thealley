package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.kasa.bulb.BulbState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
@SerialName("SDimmer")
data class SDimmerConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee",
    override val timeout: Duration = 10.minutes,
    val switchTimeout: Duration = 10.minutes,
    override val sensors: List<Int> = listOf()
) : IAlleyConfig<BulbState>,
    IZigbeeConfig<BulbState>,
    ITriggerableConfig<BulbState>,
    IAlleyLightConfig,
    IConfigEditable<SDimmerConfig> by SimpleConfigEditable(
        listOf(
            SDimmerConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            SDimmerConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            SDimmerConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) },
            SDimmerConfig::timeout.fieldEditor("Timeout") { c, n -> c.copy(timeout = n) },
            SDimmerConfig::switchTimeout.fieldEditor("Switch Timeout") { c, n -> c.copy(switchTimeout = n) },
            SDimmerConfig::sensors.fieldEditor("Sensors", { it is MotionConfig }) { c, n -> c.copy(sensors = n) }
        )
    ) {
    override val defaultState = BulbState()
    override val stateSerializer = BulbState.serializer()
}
