package uk.co.thomasc.thealley.devices.zigbee.custom

import kotlinx.coroutines.launch
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.state.zigbee.blind.BlindState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.ZBlindConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice
import uk.co.thomasc.thealley.devices.zigbee.blind.BlindCommand
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.IBlindState
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait

class ZBlindDevice(id: Int, config: ZBlindConfig, state: BlindState, stateStore: IStateUpdater<BlindState>) :
    ZigbeeDevice<BlindUpdate, ZBlindDevice, ZBlindConfig, BlindState>(id, config, state, stateStore, BlindUpdate.serializer()), IAlleyLight {

    override suspend fun onInit(bus: AlleyEventBusShim) {
        registerGoogleHomeDevice(
            DeviceType.BLINDS,
            true,
            OpenCloseTrait(
                getPosition = {
                    IBlindState.SingleDirection(state.position ?: 0)
                },
                setPosition = {
                    setPosition(bus, it)
                }
            )
        )
    }

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: BlindUpdate) {
        if (updateState(state.copy(position = update.position))) {
            bus.emit(ReportStateEvent(this))
        }
    }

    private suspend fun sendCommand(bus: AlleyEventEmitter, cmd: BlindCommand) =
        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}", "{\"state\": \"$cmd\"}"))

    private suspend fun setPosition(bus: AlleyEventEmitter, pos: Int) =
        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}", "{\"position\": \"$pos\"}"))

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

    companion object : KLogging()
}
