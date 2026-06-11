package uk.co.thomasc.thealley.google.followup

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import uk.co.thomasc.thealley.google.trait.SensorState

@Serializable
data class FollowUpResponse(
    val agentUserId: String,
    val eventId: String? = null,
    val requestId: String,
    val payload: FollowUpPayload
)

@Serializable
data class FollowUpPayload(
    val devices: FollowUpDevices
)

@Serializable
data class FollowUpDevices(
    val notifications: Map<String, Map<String, IFollowUpNotification>>? = null,
    val states: Map<String, JsonObject>? = null
)

@Serializable
sealed interface IFollowUpNotification {
    val priority: Int
}

@Serializable
data class SensorStateNotification(
    override val priority: Int,
    val name: String,
    val currentSensorState: String
) : IFollowUpNotification {
    constructor(sensorState: SensorState, state: String) : this(0, sensorState.human, state)
}
