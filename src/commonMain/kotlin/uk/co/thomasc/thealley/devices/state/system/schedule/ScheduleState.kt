package uk.co.thomasc.thealley.devices.state.system.schedule

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class ScheduleState(
    val date: LocalDate? = null,
    val state: Int? = null
) : IAlleyState
