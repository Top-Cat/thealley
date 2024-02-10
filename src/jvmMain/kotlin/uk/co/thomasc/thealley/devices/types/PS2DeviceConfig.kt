package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.ps2.PS2Device
import uk.co.thomasc.thealley.devices.ps2.PS2State

class PS2DeviceConfig(val config: PS2Config) : IAlleyDeviceConfig<PS2Device, PS2Config, PS2State>() {
    override fun create(id: Int, state: PS2State, stateStore: IStateUpdater<PS2State>, dev: AlleyDeviceMapper) = PS2Device(id, config, state, stateStore)
    override fun stateSerializer() = PS2State.serializer()
}
