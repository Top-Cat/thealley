package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.SerialName

enum class TexecomAreaStatus(val emoji: String, val tag: String, val human: String) {
    @SerialName("disarmed")
    DISARMED("\uD83D\uDD13", "unlock", "disarmed"),

    @SerialName("full_armed")
    ARMED("\uD83D\uDD12", "lock", "armed"),

    @SerialName("part_armed_1")
    PART1("1\uFE0F⃣", "one", "part armed (1)"),

    @SerialName("part_armed_2")
    PART2("2\uFE0F⃣", "two", "part armed (2)"),

    @SerialName("part_armed_3")
    PART3("3\uFE0F⃣", "three", "part armed (3)"),

    @SerialName("triggered")
    TRIGGERED("⚠", "warning", "triggered!"),

    @SerialName("in_entry")
    IN_ENTRY("\uD83D\uDEAA", "door", "in entry"),

    @SerialName("in_exit")
    IN_EXIT("\uD83D\uDEAA", "door", "in exit")
}
