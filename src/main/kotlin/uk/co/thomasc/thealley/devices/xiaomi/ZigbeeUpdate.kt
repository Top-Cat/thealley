package uk.co.thomasc.thealley.devices.xiaomi

interface ZigbeeUpdate {
    val linkquality: Int
    val battery: Int?
    val deviceTemperature: Int?
}
