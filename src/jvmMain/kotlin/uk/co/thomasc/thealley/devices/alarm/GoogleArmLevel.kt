package uk.co.thomasc.thealley.devices.alarm

enum class GoogleArmLevel(val str: String, val armLevel: String?) {
    NONE("", null),
    GARAGE("Garage", "Garage"),
    FULL("Garage,House", "FULL")
}
