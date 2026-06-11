package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.command.ISensorStateCommand
import uk.co.thomasc.thealley.google.followup.IFollowUpNotification
import uk.co.thomasc.thealley.google.trait.CurrentSensorStateData.CurrentSensorState
import uk.co.thomasc.thealley.web.google.ExecuteStatus

enum class SensorState(val human: String, val states: Set<String>? = null, val unit: String? = null) {
    AirQuality(
        "AirQuality",
        setOf("healthy", "moderate", "unhealthy", "unhealthy for sensitive groups", "very unhealthy", "hazardous", "good", "fair", "poor", "very poor", "severe", "unknown"),
        "AQI"
    ),
    CarbonMonoxideLevel(
        "CarbonMonoxideLevel",
        setOf("carbon monoxide detected", "high", "no carbon monoxide detected", "unknown"),
        "PARTS_PER_MILLION"
    ),
    SmokeLevel(
        "SmokeLevel",
        setOf("smoke detected", "high", "no smoke detected", "unknown"),
        "PARTS_PER_MILLION"
    ),
    FilterCleanliness(
        "FilterCleanliness",
        setOf("clean", "dirty", "needs replacement", "unknown")
    ),
    WaterLeak(
        "WaterLeak",
        setOf("leak", "no leak", "unknown")
    ),
    RainDetection(
        "RainDetection",
        setOf("rain detected", "no rain detected", "unknown")
    ),
    FilterLifeTime(
        "FilterLifeTime",
        setOf("new", "good", "replace soon", "replace now", "unknown"),
        "PERCENTAGE"
    ),
    PreFilterLifeTime(
        "PreFilterLifeTime",
        unit = "PERCENTAGE"
    ),
    HEPAFilterLifeTime(
        "HEPAFilterLifeTime",
        unit = "PERCENTAGE"
    ),
    Max2FilterLifeTime(
        "Max2FilterLifeTime",
        unit = "PERCENTAGE"
    ),
    CarbonDioxideLevel(
        "CarbonDioxideLevel",
        unit = "PARTS_PER_MILLION"
    ),
    PM2_5(
        "PM2.5",
        unit = "MICROGRAMS_PER_CUBIC_METER"
    ),
    PM10(
        "PM10",
        unit = "MICROGRAMS_PER_CUBIC_METER"
    ),
    VolatileOrganicCompounds(
        "VolatileOrganicCompounds",
        unit = "PARTS_PER_MILLION"
    )
}

@Serializable
data class SupportedSensorState(
    val name: String,
    val descriptiveCapabilities: DescriptiveCapabilities? = null,
    val numericCapabilities: NumericCapabilities? = null
) {
    @Serializable
    data class DescriptiveCapabilities(
        val availableStates: Set<String>
    )

    @Serializable
    data class NumericCapabilities(
        val rawValueUnit: String
    )
}

@Serializable
data class CurrentSensorStateData(val currentSensorStateData: Set<CurrentSensorState>) {
    @Serializable
    data class CurrentSensorState(val name: SensorState, val currentSensorState: String? = null, val rawValue: Float? = null, val alarmState: AlarmState? = null, val alarmSilenceState: AlarmSilenceState? = null)

    enum class AlarmState {
        IDLE, PRE_ALARM_1, PRE_ALARM_2, ALARM
    }

    enum class AlarmSilenceState {
        ALLOWED, DISALLOWED, SILENCED
    }
}

class SensorStateTrait(
    private val sensorStatesSupported: Set<SensorState> = setOf(),
    private val getNotificationState: () -> Pair<SensorState, String>,
    private val getStates: suspend () -> Set<CurrentSensorState>
) : GoogleHomeTrait<ISensorStateCommand<*>>() {
    override val name = "action.devices.traits.SensorState"
    override val klazz = ISensorStateCommand::class

    override suspend fun getAttributes() = mapOf(
        "sensorStatesSupported" to alleyJson.encodeToJsonElement(
            sensorStatesSupported.map { state ->
                SupportedSensorState(
                    state.human,
                    state.states?.let { SupportedSensorState.DescriptiveCapabilities(it) },
                    state.unit?.let { SupportedSensorState.NumericCapabilities(it) }
                )
            }
        )
    )

    override suspend fun getState() = alleyJson.encodeToJsonElement(CurrentSensorStateData(getStates())).jsonObject

    override fun getNotification() = getNotificationState().let { (type, state) ->
        name.substringAfterLast('.') to SensorStateNotification(type, state)
    }

    override suspend fun handleCommand(cmd: ISensorStateCommand<*>): ExecuteStatus {
        return ExecuteStatus.SUCCESS()
    }
}

@Serializable
data class SensorStateNotification(
    override val priority: Int,
    val name: String,
    val currentSensorState: String
) : IFollowUpNotification {
    constructor(sensorState: SensorState, state: String) : this(0, sensorState.human, state)
}
