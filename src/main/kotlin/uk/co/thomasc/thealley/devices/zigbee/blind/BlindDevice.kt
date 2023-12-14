package uk.co.thomasc.thealley.devices.zigbee.blind

import kotlinx.coroutines.launch
import mu.KLogging
import uk.co.thomasc.thealley.alleyJsonUgly
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IAlleyLight
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.BlindConfig
import uk.co.thomasc.thealley.devices.zigbee.Zigbee2MqttHelper
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.IBlindState
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait

class BlindDevice(id: Int, config: BlindConfig, state: BlindState, stateStore: IStateUpdater<BlindState>) :
    AlleyDevice<BlindDevice, BlindConfig, BlindState>(id, config, state, stateStore), IAlleyLight {

    private lateinit var zigbeeHelper: Zigbee2MqttHelper<BlindMotorUpdate>

    override suspend fun init(bus: AlleyEventBus) {
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

        zigbeeHelper = Zigbee2MqttHelper(bus, config.prefix, config.deviceId) { json ->
            alleyJsonUgly.decodeFromString<BlindMotorUpdate>(json).also {
                updateState(state.copy(position = it.position))
            }
        }

        Zigbee2MqttHelper.scope.launch {
            val res = zigbeeHelper.get() // Force initial update
            println(res)
        }
    }

    private suspend fun sendCommand(bus: AlleyEventBus, cmd: BlindCommand) =
        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", "{\"state\": \"$cmd\"}"))

    private suspend fun setPosition(bus: AlleyEventBus, pos: Int) =
        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", "{\"position\": \"$pos\"}"))

    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) =
        sendCommand(bus, if (value) BlindCommand.OPEN else BlindCommand.CLOSE)

    override suspend fun getLightState() = IAlleyLight.LightState(state.position)

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        lightState.brightness?.let {
            setPosition(bus, it)
        }
    }

    override suspend fun getPowerState() = (state.position ?: 0) > 0

    override suspend fun togglePowerState(bus: AlleyEventBus) = setPowerState(bus, !getPowerState())

    companion object : KLogging()
}
