package uk.co.thomasc.thealley.devicev2.xiaomi

interface ZigbeeUpdate {
    val linkquality: Int
    val battery: Int?
    val deviceTemperature: Int?
}
