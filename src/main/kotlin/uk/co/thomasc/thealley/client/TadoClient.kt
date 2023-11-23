package uk.co.thomasc.thealley.client

import at.topc.tado.data.common.TadoPower
import at.topc.tado.data.common.TadoTemperature
import at.topc.tado.data.common.typed.ITadoTyped
import at.topc.tado.data.common.typed.TadoTypedHeating
import at.topc.tado.data.common.typed.TadoTypedPercentage
import at.topc.tado.data.common.typed.TadoTypedTemperature

sealed interface ITadoSetting
sealed interface ITadoData

data class PercentageData(
    val percentage: Double
) : ITadoData

data class TemperatureData(
    val celsius: Double,
    val fahrenheit: Double
) : ITadoData

data class HeatingSetting(
    val power: Int,
    val temperature: TadoTemperature? = null
) : ITadoSetting

data class TransformedZoneState(
    val zone: Int,
    val tadoMode: Int,
    val setting: ITadoSetting?,
    val activityDataPoints: Map<String, ITadoData?>,
    val sensorDataPoints: Map<String, ITadoData?>
) {
    constructor(zone: Int, tadoMode: Int, setting: ITadoTyped?, activityDataPoints: Map<String, ITadoTyped>, sensorDataPoints: Map<String, ITadoTyped>) :
            this(zone, tadoMode, mapSetting(setting), activityDataPoints.mapValues(::mapDataPoints), sensorDataPoints.mapValues(::mapDataPoints))

    companion object {
        fun mapDataPoints(node: Map.Entry<String, ITadoTyped>) = when (val nv = node.value) {
            is TadoTypedPercentage -> PercentageData(nv.percentage)
            is TadoTypedTemperature -> TemperatureData(nv.celsius, nv.fahrenheit)
            else -> null
        }

        fun mapSetting(setting: ITadoTyped?) = when (setting) {
            is TadoTypedHeating -> HeatingSetting(if (setting.power == TadoPower.ON) 1 else 0, setting.temperature)
            else -> null
        }
    }
}
