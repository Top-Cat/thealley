package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.Serializable

@Serializable
sealed interface IAlleyConfig {
    val name: String
    fun deviceConfig(): IAlleyDeviceConfig<*, *, *>
}
