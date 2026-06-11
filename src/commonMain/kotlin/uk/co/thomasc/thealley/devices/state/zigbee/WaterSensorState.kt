package uk.co.thomasc.thealley.devices.state.zigbee

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class WaterSensorState(val battery: Int? = null, val alarmState: Boolean = false, val lowBatNotificationState: Boolean = false) : IZigbeeState, IAlleyState
