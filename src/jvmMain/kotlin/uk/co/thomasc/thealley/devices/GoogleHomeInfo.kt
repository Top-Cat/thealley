package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.GoogleHomeTrait
import uk.co.thomasc.thealley.web.google.AlleyDeviceInfo

data class GoogleHomeInfo(
    val type: DeviceType,
    val traits: Set<GoogleHomeTrait<*>>,
    val willReportState: Boolean = false,
    val deviceInfo: (() -> AlleyDeviceInfo)? = null
)
