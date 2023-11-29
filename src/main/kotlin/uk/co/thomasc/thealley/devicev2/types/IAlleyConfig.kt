package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.Serializable

@Serializable
sealed interface IAlleyConfig {
    val name: String
    fun deviceConfig(): IAlleyDeviceConfig<*, *, *>
}
