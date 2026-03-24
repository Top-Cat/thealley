package uk.co.thomasc.thealley.devices.zigbee.aq2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeTemperature
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateBattery

@Serializable
data class MotionSensorUpdate(
    // Generic
    override val linkquality: Int,
    override val battery: Float? = null,
    @SerialName("device_temperature")
    override val temperature: Float? = null,

    // Light sensor
    val voltage: Int = 0,

    val illuminance: Int = 0,
    @SerialName("illuminance_lux")
    val illuminanceLux: Int = 0,
    val occupancy: Boolean = false,
    @SerialName("power_outage_count")
    val powerOutageCount: Int = 0
) : ZigbeeUpdateBattery, ZigbeeTemperature
