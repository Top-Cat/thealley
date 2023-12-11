package uk.co.thomasc.thealley.web.google

import kotlinx.serialization.SerialName

enum class GoogleHomeErrorCode {
    @SerialName("alreadyAtMax")
    AlreadyAtMax,

    @SerialName("deviceNotFound")
    DeviceNotFound,

    @SerialName("deviceOffline")
    DeviceOffline,

    @SerialName("deviceTurnedOff")
    DeviceTurnedOff,

    @SerialName("functionNotSupported")
    FunctionNotSupported,

    @SerialName("networkSpeedTestInProgress")
    NetworkSpeedTestInProgress,

    @SerialName("transientError")
    TransientError
}
