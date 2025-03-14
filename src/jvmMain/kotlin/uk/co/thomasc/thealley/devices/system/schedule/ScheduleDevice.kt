package uk.co.thomasc.thealley.devices.system.schedule

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.state.system.schedule.ScheduleState
import uk.co.thomasc.thealley.devices.system.TickEvent
import uk.co.thomasc.thealley.devices.types.ScheduleConfig

class ScheduleDevice(id: Int, config: ScheduleConfig, state: ScheduleState, stateStore: IStateUpdater<ScheduleState>, val dev: AlleyDeviceMapper) :
    AlleyDevice<ScheduleDevice, ScheduleConfig, ScheduleState>(id, config, state, stateStore) {

    private val sortedStates = config.elements.sortedBy { it.time }

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<TickEvent> { ev ->
            val now = ev.now.toLocalDateTime(TimeZone.UTC)
            val idealState = sortedStates.indexOfLast { it.time < now.time }.let {
                if (it == -1) sortedStates.lastIndex else it
            }

            if (state.date != now.date || state.state != idealState) {
                val newState = sortedStates[idealState]
                val device = dev.getDevice(config.device)

                if (device is IAlleyRelay) {
                    device.setPowerState(bus, newState.state)
                }

                updateState(state.copy(state = idealState, date = now.date))
            }
        }
    }
}
