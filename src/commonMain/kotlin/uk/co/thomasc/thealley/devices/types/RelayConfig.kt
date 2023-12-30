package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
@SerialName("Relay")
data class RelayConfig(
    override val name: String,
    val host: String,
    val apiKey: String,
    val timeout: Duration = 10.minutes,
    val switchTimeout: Duration = 10.minutes,
    val sensors: List<Int> = listOf()
) : IAlleyConfig
