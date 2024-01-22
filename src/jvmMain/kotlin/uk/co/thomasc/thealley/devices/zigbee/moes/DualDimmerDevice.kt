package uk.co.thomasc.thealley.devices.zigbee.moes

import kotlinx.coroutines.delay
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.generic.IAlleyMultiGangLight
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.DualDimmerConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice
import uk.co.thomasc.thealley.devices.zigbee.relay.MultiGangUpdate
import uk.co.thomasc.thealley.devices.zigbee.relay.ZMultiLightSet
import uk.co.thomasc.thealley.devices.zigbee.relay.ZMultiRelaySet
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction

class DualDimmerDevice(id: Int, config: DualDimmerConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeDevice<DualDimmerUpdate, DualDimmerDevice, DualDimmerConfig, EmptyState>(id, config, state, stateStore, DualDimmerUpdate.serializer()), IAlleyMultiGangLight {

    override fun getEvent() = MqttSendEvent("${config.prefix}/${config.deviceId}/set", "{\"countdown_l1\": \"0\"}")

    private suspend fun setLightState(bus: AlleyEventBus, index: Int, state: ZRelayAction) {
        val json = when (index) {
            2 -> ZMultiRelaySet(state2 = state)
            else -> ZMultiRelaySet(state1 = state)
        }.toJson()

        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", json))
    }

    private suspend fun setLightBrightness(bus: AlleyEventBus, index: Int, brightness: Int) {
        val currentState = getPowerState(index)
        if (brightness == 0) {
            if (currentState) setLightState(bus, index, ZRelayAction.OFF)
            return
        } else if (!currentState) { // Turn on before changing brightness
            setLightState(bus, index, ZRelayAction.ON)
            waitForUpdate()
            delay(3000)
        }

        val json = when {
            index == 2 -> ZMultiLightSet(brightness2 = brightness)
            else -> ZMultiLightSet(brightness1 = brightness)
        }.toJson()

        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", json))
    }

    override suspend fun getLightState(index: Int) =
        getState().let { state ->
            IAlleyLight.LightState(
                when (index) {
                    2 -> state.brightness2
                    else -> state.brightness1
                }
            )
        }

    override suspend fun setComplexState(bus: AlleyEventBus, index: Int, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        setLightBrightness(bus, index, lightState.brightness ?: 0)
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

    override suspend fun onUpdate(bus: AlleyEventBus, update: DualDimmerUpdate) {
        bus.emit(MultiGangUpdate(id))
    }
}
