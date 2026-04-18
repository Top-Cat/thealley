package uk.co.thomasc.thealley.devices.zigbee.moes

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.zigbee.moes.SceneSwitchState
import uk.co.thomasc.thealley.devices.types.SceneSwitchConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice
import uk.co.thomasc.thealley.devices.zigbee.blind.BlindCommand
import uk.co.thomasc.thealley.devices.zigbee.custom.ZBlindDevice

class SceneSwitchDevice(id: Int, config: SceneSwitchConfig, state: SceneSwitchState, stateStore: IStateUpdater<SceneSwitchState>, val dev: AlleyDeviceMapper) :
    ZigbeeDevice<SceneSwitchUpdate, SceneSwitchDevice, SceneSwitchConfig, SceneSwitchState>(id, config, state, stateStore, SceneSwitchUpdate.serializer()) {

    val targets = listOf(
        config.targets0,
        config.targets1,
        config.targets2
    )

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: SceneSwitchUpdate) {
        update.action?.let { action ->
            updateState(state.copy(state = state.state.plus(action.button to !state.state.getOrDefault(action.button, false))))

            targets.getOrNull(action.button - 1)?.let { targetIds ->
                val devices = targetIds.map { dev.getDevice(it) }

                devices.forEach { device ->
                    if (device is ZBlindDevice) {
                        val cmd = when {
                            action.times > 1 -> BlindCommand.STOP
                            action.button == 1 -> BlindCommand.OPEN
                            else -> BlindCommand.CLOSE
                        }
                        device.sendCommand(bus, cmd)
                    }

                    // TODO: Other device types
                }
            }
        }
    }
}
