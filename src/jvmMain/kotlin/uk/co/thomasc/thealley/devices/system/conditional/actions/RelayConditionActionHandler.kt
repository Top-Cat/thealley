package uk.co.thomasc.thealley.devices.system.conditional.actions

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IAlleyRelay

class RelayConditionActionHandler(val action: RelayConditionAction) : IConditionActionHandler {
    override suspend fun perform(dev: AlleyDeviceMapper, bus: AlleyEventBus) {
        val device = dev.getDevice(action.deviceId)
        if (device is IAlleyRelay) {
            device.setPowerState(bus, action.state)
        }
    }
}
