package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.schedule.ScheduleDevice
import uk.co.thomasc.thealley.devices.system.schedule.ScheduleState

class ScheduleDeviceConfig(val config: ScheduleConfig) : IAlleyDeviceConfig<ScheduleDevice, ScheduleConfig, ScheduleState>() {
    override fun create(id: Int, state: ScheduleState, stateStore: IStateUpdater<ScheduleState>, dev: AlleyDeviceMapper) = ScheduleDevice(id, config, state, stateStore, dev)
    override fun stateSerializer() = ScheduleState.serializer()
}
