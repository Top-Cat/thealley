package uk.co.thomasc.thealley.devices.system.conditional.actions

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.onkyo.OnkyoDevice

class OnkyoConditionActionHandler(val action: OnkyoConditionAction) : IConditionActionHandler {
    override suspend fun perform(dev: AlleyDeviceMapper, bus: AlleyEventEmitter) {
        val device = dev.getDevice(action.deviceId)
        if (device is OnkyoDevice) {
            device.setPowerState(true)
            action.input?.let { device.setInput(it) }
            action.volume?.let { device.setVolume(it) }
        }
    }
}
