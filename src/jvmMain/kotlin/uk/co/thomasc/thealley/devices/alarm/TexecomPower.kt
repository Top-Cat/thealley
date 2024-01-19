package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TexecomPower(
    @SerialName("battery_charging_current")
    val batteryChargingCurrent: Int,
    @SerialName("battery_voltage")
    val batteryVoltage: Float,
    @SerialName("panel_current")
    val panelCurrent: Int,
    @SerialName("panel_voltage")
    val panelVoltage: Float
)
