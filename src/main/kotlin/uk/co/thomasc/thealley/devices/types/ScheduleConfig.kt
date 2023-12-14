package uk.co.thomasc.thealley.devices.types

import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.schedule.ScheduleDevice
import uk.co.thomasc.thealley.devices.system.schedule.ScheduleState

@Serializable
@SerialName("Schedule")
data class ScheduleConfig(
    override val name: String,
    val device: Int,
    val elements: List<ScheduleElement> = listOf()
) : IAlleyConfig {
    override fun deviceConfig() = ScheduleDeviceConfig(this)

    @Serializable
    data class ScheduleElement(
        val time: LocalTime,
        val state: Boolean
    )

    class ScheduleDeviceConfig(val config: ScheduleConfig) : IAlleyDeviceConfig<ScheduleDevice, ScheduleConfig, ScheduleState>() {
        override fun create(id: Int, state: ScheduleState, stateStore: IStateUpdater<ScheduleState>, dev: AlleyDeviceMapper) = ScheduleDevice(id, config, state, stateStore, dev)
        override fun stateSerializer() = ScheduleState.serializer()
    }
}
