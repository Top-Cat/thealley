package uk.co.thomasc.thealley.devices.somfy

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.SomfyBlindConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.IBlindState
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait

class SomfyBlindDevice(id: Int, config: SomfyBlindConfig, state: SomfyBlindState, stateStore: IStateUpdater<SomfyBlindState>) :
    AlleyDevice<SomfyBlindDevice, SomfyBlindConfig, SomfyBlindState>(id, config, state, stateStore), IAlleyLight {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            val parts = ev.topic.split('/')

            if (parts.size < 2 || parts[0] != config.prefix) return@handle

            if (parts.size >= 4 && parts[1] == "shades" && parts[2] == config.deviceId) {
                // State update
                updateState {
                    when (parts[3]) {
                        "position" -> it.copy(position = ev.payload.toIntOrNull() ?: it.position)
                        else -> it
                    }
                }.let {
                    if (it) {
                        bus.emit(ReportStateEvent(this))
                    }
                }
            }
        }

        registerGoogleHomeDevice(
            DeviceType.BLINDS,
            false,
            OpenCloseTrait(
                getPosition = {
                    IBlindState.SingleDirection(state.position)
                },
                setPosition = {
                    setPosition(bus, it)
                }
            )
        )
    }

    companion object : KLogging()

    private suspend fun setPosition(bus: AlleyEventBus, position: Int) {
        bus.emit(MqttSendEvent("${config.prefix}/shades/${config.deviceId}/target/set", position.toString()))
    }

    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) {
        setPosition(bus, if (value) 0 else 100)
    }

    override suspend fun getLightState() = IAlleyLight.LightState(state.position)

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        lightState.brightness?.let {
            setPosition(bus, it)
        }
    }

    override suspend fun getPowerState() = state.position <= 0

    override suspend fun togglePowerState(bus: AlleyEventBus) = setPowerState(bus, !getPowerState())
}
