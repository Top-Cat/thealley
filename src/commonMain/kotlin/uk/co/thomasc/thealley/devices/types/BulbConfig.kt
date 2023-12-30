package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
@SerialName("Bulb")
data class BulbConfig(
    override val name: String,
    override val host: String,
    val timeout: Duration = 10.minutes,
    val switchTimeout: Duration = 10.minutes,
    val sensors: List<Int> = listOf()
) : IAlleyConfig, IKasaConfig
