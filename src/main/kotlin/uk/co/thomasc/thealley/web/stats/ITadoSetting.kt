package uk.co.thomasc.thealley.web.stats

import at.topc.tado.data.common.TadoTemperature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ITadoSetting

@Serializable
@SerialName("heating")
data class HeatingSetting(
    val power: Int,
    val temperature: TadoTemperature? = null
) : ITadoSetting
