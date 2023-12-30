package uk.co.thomasc.thealley.web.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ITadoData

@Serializable
@SerialName("percentage")
data class PercentageData(
    val percentage: Double
) : ITadoData

@Serializable
@SerialName("temperature")
data class TemperatureData(
    val celsius: Double,
    val fahrenheit: Double
) : ITadoData
