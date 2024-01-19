package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.SerialName

enum class TexecomAreaStatus {
    @SerialName("disarmed")
    DISARMED,

    @SerialName("full_armed")
    ARMED,

    @SerialName("part_armed_1")
    PART1,

    @SerialName("part_armed_2")
    PART2,

    @SerialName("part_armed_3")
    PART3,

    @SerialName("triggered")
    TRIGGERED,

    @SerialName("in_entry")
    IN_ENTRY,

    @SerialName("in_exit")
    IN_EXIT
}
