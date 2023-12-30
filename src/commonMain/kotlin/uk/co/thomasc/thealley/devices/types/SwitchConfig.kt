package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Switch")
data class SwitchConfig(override val name: String, val id: Int, val scenes: List<Int>) : IAlleyConfig
