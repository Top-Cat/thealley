package uk.co.thomasc.thealley.devices.zigbee.blind

import kotlinx.coroutines.launch
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.BlindConfig
import uk.co.thomasc.thealley.devices.zigbee.Zigbee2MqttHelper
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.IBlindState
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait

class BlindDevice(id: Int, config: BlindConfig, state: BlindState, stateStore: IStateUpdater<BlindState>) :
    ZigbeeDevice<BlindMotorUpdate, BlindDevice, BlindConfig, BlindState>(id, config, state, stateStore, BlindMotorUpdate.serializer()), IAlleyLight {

    override suspend fun onInit(bus: AlleyEventBusShim) {
        registerGoogleHomeDevice(
            DeviceType.BLINDS,
            false,
            OpenCloseTrait(
                getPosition = {
                    IBlindState.SingleDirection(state.position ?: 0)
                },
                setPosition = {
                    setPosition(bus, it)
                }
            )
        )

        Zigbee2MqttHelper.scope.launch {
            getState() // Force initial update
        }
    }

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: BlindMotorUpdate) {
        // Do nothing
    }

    private suspend fun sendCommand(bus: AlleyEventEmitter, cmd: BlindCommand) =
        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", "{\"state\": \"$cmd\"}"))

    private suspend fun setPosition(bus: AlleyEventEmitter, pos: Int) =
        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", "{\"position\": \"$pos\"}"))

    override suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean) =
        sendCommand(bus, if (value) BlindCommand.OPEN else BlindCommand.CLOSE)

    override suspend fun getLightState() = IAlleyLight.LightState(state.position)

    override suspend fun setComplexState(bus: AlleyEventEmitter, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        lightState.brightness?.let {
            setPosition(bus, it)
        }
    }

    override suspend fun getPowerState() = (state.position ?: 0) > 0

    override suspend fun togglePowerState(bus: AlleyEventEmitter) = setPowerState(bus, !getPowerState())
}
