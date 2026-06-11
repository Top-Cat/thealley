package uk.co.thomasc.thealley.devices.zigbee.sonoff

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTANew
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAStatus
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateBattery

@Serializable
data class WaterSensorUpdate(
    override val battery: Float,
    @SerialName("battery_low")
    val batteryLow: Boolean,
    @SerialName("last_seen")
    override val lastSeen: Instant? = null,
    override val linkquality: Int,
    override val update: ZigbeeOTAStatus,
    @SerialName("water_leak")
    val waterLeak: Boolean
) : ZigbeeUpdateBattery, ZigbeeOTANew
