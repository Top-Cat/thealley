package uk.co.thomasc.thealley.devices.alarm

enum class AreaCommand(val state: String) {
    FULL("full_arm"),
    PART1("part_arm_1"),
    PART2("part_arm_2"),
    PART3("part_arm_3"),
    DISARM("disarm")
}
