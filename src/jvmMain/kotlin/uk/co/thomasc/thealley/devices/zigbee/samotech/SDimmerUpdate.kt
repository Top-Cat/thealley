package uk.co.thomasc.thealley.devices.zigbee.samotech

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import uk.co.thomasc.thealley.devices.zigbee.ZigbeePowerMonitoring
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateMains
import uk.co.thomasc.thealley.devices.zigbee.moes.ZigbeePowerOnBehavior
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateDimmer

@Serializable
data class SDimmerUpdate(
    override val linkquality: Int,
    override val voltage: Float,
    override val current: Float,
    override val energy: Float,
    override val power: Float,
    @SerialName("power_on_behavior")
    val powerOnBehavior: ZigbeePowerOnBehavior,

    @SerialName("level_config")
    val levelConfig: ZigbeeLevelConfig? = null,

    override val brightness: Int,
    override val state: ZRelayAction
) : ZigbeeUpdateMains, ZigbeePowerMonitoring, ZigbeeUpdateDimmer

@Serializable
data class ZigbeeLevelConfig(
    @SerialName("on_level")
    val onLevel: JsonElement
)
