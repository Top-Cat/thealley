package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.system.schedule.ScheduleState
import uk.co.thomasc.thealley.devices.system.schedule.ScheduleDevice

class ScheduleDeviceConfig(config: ScheduleConfig) : IAlleyDeviceConfig<ScheduleDevice, ScheduleConfig, ScheduleState>(config) {
    override fun create(id: Int, state: ScheduleState, stateStore: IStateUpdater<ScheduleState>, dev: AlleyDeviceMapper) = ScheduleDevice(id, config, state, stateStore, dev)
}
