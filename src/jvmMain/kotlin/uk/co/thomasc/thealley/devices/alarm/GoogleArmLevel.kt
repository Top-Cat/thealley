package uk.co.thomasc.thealley.devices.alarm

enum class GoogleArmLevel(val str: String, val armLevel: String?) {
    NONE("", null),
    GARAGE("Garage", "Garage"),
    HOUSE("House", "House"),
    FULL("Garage,House", "FULL")
}
