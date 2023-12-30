package uk.co.thomasc.thealley.devices.types

interface IZigbeeConfig : IAlleyConfig {
    val prefix: String
    val deviceId: String
}
