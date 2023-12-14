package uk.co.thomasc.thealley.devices.zigbee

interface ZigbeeUpdate {
    val linkquality: Int
    val deviceTemperature: Int?
}

interface ZigbeeUpdateBattery : ZigbeeUpdate {
    val battery: Int?
}

interface ZigbeeUpdateMains : ZigbeeUpdate {
    val voltage: Float
}

interface ZigbeePowerMonitoring {
    val current: Float
    val energy: Float
    val power: Float
}
