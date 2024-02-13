package uk.co.thomasc.thealley.web.stats

import at.topc.tado.data.common.TadoPower
import at.topc.tado.data.common.typed.ITadoTyped
import at.topc.tado.data.common.typed.TadoTypedHeating
import at.topc.tado.data.common.typed.TadoTypedPercentage
import at.topc.tado.data.common.typed.TadoTypedTemperature
import kotlinx.serialization.Serializable

@Serializable
data class TransformedZoneState(
    val home: String,
    val zone: Int,
    val tadoMode: Int,
    val setting: ITadoSetting?,
    val activityDataPoints: Map<String, ITadoData?>,
    val sensorDataPoints: Map<String, ITadoData?>
) {
    constructor(home: String, zone: Int, tadoMode: Int, setting: ITadoTyped?, activityDataPoints: Map<String, ITadoTyped>, sensorDataPoints: Map<String, ITadoTyped>) :
        this(home, zone, tadoMode, mapSetting(setting), activityDataPoints.mapValues(Companion::mapDataPoints), sensorDataPoints.mapValues(Companion::mapDataPoints))

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
