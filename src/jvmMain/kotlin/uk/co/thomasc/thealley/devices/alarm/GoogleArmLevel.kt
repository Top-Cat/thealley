package uk.co.thomasc.thealley.devices.alarm

enum class GoogleArmLevel(val str: String, val armLevel: String?, val areas: Set<String>) {
    NONE("", null, emptySet()),
    GARAGE("Garage", "Garage", setOf("B")),
    FULL("Garage,House", "FULL", setOf("A", "B"))
}
