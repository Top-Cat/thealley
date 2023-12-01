package uk.co.thomasc.thealley.devicev2.xiaomi.aq2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.xiaomi.ZigbeeUpdate

@Serializable
data class MotionSensorUpdate(
    // Generic
    override val linkquality: Int,
    override val battery: Int? = null,
    @SerialName("device_temperature")
    override val deviceTemperature: Int? = null,

    // Light sensor
    val voltage: Int = 0,

    val illuminance: Int = 0,
    @SerialName("illuminance_lux")
    val illuminanceLux: Int = 0,
    val occupancy: Boolean = false,
    @SerialName("power_outage_count")
    val powerOutageCount: Int = 0
) : ZigbeeUpdate
