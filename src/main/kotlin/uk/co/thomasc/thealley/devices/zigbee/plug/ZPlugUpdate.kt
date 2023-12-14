package uk.co.thomasc.thealley.devices.zigbee.plug

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import uk.co.thomasc.thealley.devices.zigbee.ZigbeePowerMonitoring
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateMains

@Serializable
data class ZPlugUpdate(
    override val linkquality: Int,
    @SerialName("device_temperature")
    override val deviceTemperature: Int? = null,

    override val current: Float,
    override val energy: Float,
    override val power: Float,
    override val voltage: Float,

    @SerialName("power_outage_memory")
    val powerOutageMemory: ZigbeePOM,
    val state: ZPlugAction,
    @SerialName("child_lock")
    val childLock: ZPlugChildLock,
    @SerialName("indicator_mode")
    val indicatorMode: ZPlugIndicator,

    @SerialName("update_available")
    val updateAvailable: JsonElement,
    val update: JsonElement
) : ZigbeeUpdateMains, ZigbeePowerMonitoring
