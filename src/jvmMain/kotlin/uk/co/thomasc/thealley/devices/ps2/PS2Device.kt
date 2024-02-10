package uk.co.thomasc.thealley.devices.ps2

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.PS2Config
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait

enum class ESPHomeSwitchState {
    ON, OFF, TOGGLE
}

class PS2Device(id: Int, config: PS2Config, state: PS2State, stateStore: IStateUpdater<PS2State>) :
    AlleyDevice<PS2Device, PS2Config, PS2State>(id, config, state, stateStore), IAlleyRelay {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            val parts = ev.topic.split('/')

            if (parts.size < 2 || parts[0] != config.prefix) return@handle

            if (parts.size >= 4 && parts[3] == "state") {
                // State update
                updateState {
                    when (parts[2]) {
                        "power" -> it.copy(power = ev.payload == "ON")
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
            DeviceType.OUTLET,
            true,
            OnOffTrait(
                getOnOff = ::getPowerState,
                setOnOff = {
                    setPowerState(bus, it)
                }
            )
        )
    }

    companion object : KLogging()

    private suspend fun setPower(bus: AlleyEventBus, state: ESPHomeSwitchState) {
        bus.emit(MqttSendEvent("${config.prefix}/switch/power/command", state.toString()))
    }

    private suspend fun toggleTray(bus: AlleyEventBus) {
        bus.emit(MqttSendEvent("${config.prefix}/button/tray/command", "PRESS"))
    }

    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) {
        setPower(bus, if (value) ESPHomeSwitchState.ON else ESPHomeSwitchState.OFF)
    }

    override suspend fun getPowerState() = state.power

    override suspend fun togglePowerState(bus: AlleyEventBus) {
        setPower(bus, ESPHomeSwitchState.TOGGLE)
    }
}
