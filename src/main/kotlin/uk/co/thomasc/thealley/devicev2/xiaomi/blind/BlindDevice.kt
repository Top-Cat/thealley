package uk.co.thomasc.thealley.devicev2.xiaomi.blind

import mu.KLogging
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IAlleyLight
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devicev2.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devicev2.types.BlindConfig

class BlindDevice(id: Int, config: BlindConfig, state: BlindState, stateStore: IStateUpdater<BlindState>) :
    AlleyDevice<BlindDevice, BlindConfig, BlindState>(id, config, state, stateStore), IAlleyLight {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            val host = ev.topic.substring(0, ev.topic.indexOf("/"))
            if (host != "zigbee") return@handle

            val deviceId = ev.topic.substring(host.length + 1)
            if (deviceId != config.deviceId) return@handle

            val update = alleyJson.decodeFromString<BlindMotorUpdate>(ev.payload)

            updateState(state.copy(position = update.position))
        }

        // TODO: Run get on interval?
        bus.emit(MqttSendEvent("zigbee/${config.deviceId}/get", "{\"state\": \"\"}"))
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
