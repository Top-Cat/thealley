package uk.co.thomasc.thealley.devices.types

import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Schedule")
data class ScheduleConfig(
    override val name: String,
    val device: Int,
    val elements: List<ScheduleElement> = listOf()
) : IAlleyConfig {
    @Serializable
    data class ScheduleElement(
        val time: LocalTime,
        val state: Boolean
    )
}
