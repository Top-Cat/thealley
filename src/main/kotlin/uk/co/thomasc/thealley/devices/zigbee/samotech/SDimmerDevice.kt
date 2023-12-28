package uk.co.thomasc.thealley.devices.zigbee.samotech

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IAlleyLight
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.SDimmerConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeRelayDevice

class SDimmerDevice(id: Int, config: SDimmerConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeRelayDevice<SDimmerUpdate, SDimmerDevice, SDimmerConfig, EmptyState>(id, config, state, stateStore, SDimmerUpdate.serializer()), IAlleyLight {

    override suspend fun onUpdate(bus: AlleyEventBus, state: SDimmerUpdate) {
        bus.emit(ReportStateEvent(this))
    }

    override suspend fun getLightState() = IAlleyLight.LightState(getState().brightness)

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        val json = ZDimmerSet(lightState.brightness ?: 0, null, transitionTime?.let { it / 1000f }).toJson()
        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", json))
    }
}
