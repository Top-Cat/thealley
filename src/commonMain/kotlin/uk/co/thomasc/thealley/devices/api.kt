package uk.co.thomasc.thealley.devices

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.types.IAlleyConfig

@Serializable
data class AlleyDeviceConfig(val id: Int, val config: IAlleyConfig)
