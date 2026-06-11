package uk.co.thomasc.thealley.devices.zigbee.sonoff

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.zigbee.WaterSensorState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.WaterSensorConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice
import uk.co.thomasc.thealley.devices.zigbee.custom.LowBatteryEvent
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.CurrentSensorStateData
import uk.co.thomasc.thealley.google.trait.SensorState
import uk.co.thomasc.thealley.google.trait.SensorStateTrait

class WaterSensorDevice(id: Int, config: WaterSensorConfig, state: WaterSensorState, stateStore: IStateUpdater<WaterSensorState>) :
    ZigbeeDevice<WaterSensorUpdate, WaterSensorDevice, WaterSensorConfig, WaterSensorState>(id, config, state, stateStore, WaterSensorUpdate.serializer()) {

    override suspend fun onInit(bus: AlleyEventBusShim) {
        registerGoogleHomeDevice(
            DeviceType.SENSOR,
            true,
            SensorStateTrait(
                setOf(SensorState.WaterLeak),
                {
                    SensorState.WaterLeak to if (state.alarmState) "leak" else "no leak"
                }
            ) {
                setOf(
                    CurrentSensorStateData.CurrentSensorState(
                        SensorState.WaterLeak,
                        if (state.alarmState) "leak" else "no leak",
                        alarmState = if (state.alarmState) CurrentSensorStateData.AlarmState.ALARM else CurrentSensorStateData.AlarmState.IDLE
                    )
                )
            }
        )
    }

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: WaterSensorUpdate) {
        if (update.battery < 20 && updateState(state.copy(lowBatNotificationState = true))) {
            bus.emit(LowBatteryEvent(id, update.battery))
        } else if (update.battery > 50) {
            updateState(state.copy(lowBatNotificationState = false))
        }

        val notifications = if (updateState(state.copy(alarmState = update.waterLeak)) && update.waterLeak) {
            gh?.traits?.mapNotNull { it.getNotification() }?.toMap()
        } else {
            null
        }

        bus.emit(ReportStateEvent(this, notifications))
    }

    companion object : KLogging()
}
