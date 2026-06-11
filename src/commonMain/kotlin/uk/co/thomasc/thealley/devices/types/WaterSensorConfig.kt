package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.zigbee.WaterSensorState

@Serializable
@SerialName("WaterSensor")
data class WaterSensorConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig<WaterSensorState>,
    IZigbeeConfig<WaterSensorState>,
    IConfigEditable<WaterSensorConfig> by SimpleConfigEditable(
        listOf(
            WaterSensorConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            WaterSensorConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = WaterSensorState()
    override val stateSerializer = WaterSensorState.serializer()
}
