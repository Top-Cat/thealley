package uk.co.thomasc.thealley.devices.system.schedule

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleState(
    val date: LocalDate? = null,
    val state: Int? = null
)
