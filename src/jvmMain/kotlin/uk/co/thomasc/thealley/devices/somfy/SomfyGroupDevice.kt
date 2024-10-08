package uk.co.thomasc.thealley.devices.somfy

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.state.somfy.SomfyGroupState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.SomfyGroupConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.IBlindState
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait

class SomfyGroupDevice(id: Int, config: SomfyGroupConfig, state: SomfyGroupState, stateStore: IStateUpdater<SomfyGroupState>) :
    AlleyDevice<SomfyGroupDevice, SomfyGroupConfig, SomfyGroupState>(id, config, state, stateStore), IAlleyRelay {

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<MqttMessageEvent> { ev ->
            val parts = ev.topic.split('/')

            if (parts.size < 4 || parts[0] != config.prefix || parts[1] != TOPIC || parts[2] != config.deviceId) return@handle

            // State update
            updateState {
                when (parts[3]) {
                    "direction" -> it.copy(
                        position = when (ev.payload.toIntOrNull()) {
                            -1 -> true // Opening
                            0 -> it.position
                            else -> false
                        }
                    )
                    else -> it
                }
            }.let {
                if (it) {
                    bus.emit(ReportStateEvent(this))
                }
            }
        }

        registerGoogleHomeDevice(
            DeviceType.BLINDS,
            true,
            OpenCloseTrait(
                true, // Can't set midway position
                getPosition = {
                    IBlindState.SingleDirection(if (state.position) 100 else 0)
                },
                setPosition = {
                    setPowerState(bus, it > 0)
                }
            )
        )
    }

    companion object : KLogging() {
        const val TOPIC = "groups"
    }

    private suspend fun setPosition(bus: AlleyEventEmitter, position: Int) {
        bus.emit(MqttSendEvent("${config.prefix}/$TOPIC/${config.deviceId}/direction/set", position.toString()))
    }

    override suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean) {
        setPosition(bus, if (value) -1 else 1)
    }

    override suspend fun getPowerState() = state.position

    override suspend fun togglePowerState(bus: AlleyEventEmitter) = setPowerState(bus, !getPowerState())
}
