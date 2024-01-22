package uk.co.thomasc.thealley.devices.zigbee.moes

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyMultiGangRelay
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.DualSwitchConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice
import uk.co.thomasc.thealley.devices.zigbee.relay.MultiGangUpdate
import uk.co.thomasc.thealley.devices.zigbee.relay.ZMultiRelaySet
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction

class DualSwitchDevice(id: Int, config: DualSwitchConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeDevice<DualSwitchUpdate, DualSwitchDevice, DualSwitchConfig, EmptyState>(id, config, state, stateStore, DualSwitchUpdate.serializer()), IAlleyMultiGangRelay {

    private suspend fun setLightState(bus: AlleyEventBus, index: Int, state: ZRelayAction) {
        val json = when (index) {
            2 -> ZMultiRelaySet(state2 = state)
            else -> ZMultiRelaySet(state1 = state)
        }.toJson()

        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", json))
    }

    override suspend fun setPowerState(bus: AlleyEventBus, index: Int, value: Boolean) =
        setLightState(bus, index, if (value) ZRelayAction.ON else ZRelayAction.OFF)

    override suspend fun getPowerState(index: Int) =
        getState().let { state ->
            when (index) {
                2 -> state.state2
                else -> state.state1
            } == ZRelayAction.ON
        }

    override suspend fun togglePowerState(bus: AlleyEventBus, index: Int) =
        setLightState(bus, index, ZRelayAction.TOGGLE)

    override suspend fun onUpdate(bus: AlleyEventBus, update: DualSwitchUpdate) {
        bus.emit(MultiGangUpdate(id))
    }
}
