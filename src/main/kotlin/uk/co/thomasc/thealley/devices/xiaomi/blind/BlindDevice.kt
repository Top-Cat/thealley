package uk.co.thomasc.thealley.devices.xiaomi.blind

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IAlleyLight
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.BlindConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.IBlindState
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait

class BlindDevice(id: Int, config: BlindConfig, state: BlindState, stateStore: IStateUpdater<BlindState>) :
    AlleyDevice<BlindDevice, BlindConfig, BlindState>(id, config, state, stateStore), IAlleyLight {

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

        bus.handle<MqttMessageEvent> { ev ->
            val host = ev.topic.substring(0, ev.topic.indexOf("/"))
            if (host != "zigbee") return@handle

            val deviceId = ev.topic.substring(host.length + 1)
            if (deviceId != config.deviceId) return@handle

            val update = alleyJson.decodeFromString<BlindMotorUpdate>(ev.payload)

            updateState(state.copy(position = update.position))
        }

        // TODO: Run get on interval?
        GlobalScope.launch {
            bus.emit(MqttSendEvent("zigbee/${config.deviceId}/get", "{\"state\": \"\"}"))
        }
    }

    private suspend fun sendCommand(bus: AlleyEventBus, cmd: BlindCommand) =
        bus.emit(MqttSendEvent("zigbee/${config.deviceId}/set", "{\"state\": \"$cmd\"}"))

    private suspend fun setPosition(bus: AlleyEventBus, pos: Int) =
        bus.emit(MqttSendEvent("zigbee/${config.deviceId}/set", "{\"position\": \"$pos\"}"))

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

    override suspend fun hold() {
        // TODO: revoke override so rules can change light state
    }

    override suspend fun revoke() {
        // TODO: revoke override so rules can change light state
    }

    companion object : KLogging()
}
