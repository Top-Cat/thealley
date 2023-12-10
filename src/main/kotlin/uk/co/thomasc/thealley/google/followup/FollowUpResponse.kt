package uk.co.thomasc.thealley.google.followup

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
    val notifications: Map<String, Map<String, FollowUpNotification>>? = null,
    val states: Map<String, JsonObject>? = null
)

@Serializable
data class FollowUpNotification(
    val priority: Int,
    val followUpResponse: JsonObject
)
