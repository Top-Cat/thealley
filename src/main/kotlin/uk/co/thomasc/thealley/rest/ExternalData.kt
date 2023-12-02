@file:OptIn(ExperimentalSerializationApi::class)

package uk.co.thomasc.thealley.rest

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

@Serializable
data class GoogleHomeReq(val requestId: String, val inputs: List<GoogleHomeIntent>)

@Serializable
@JsonClassDiscriminator("intent")
sealed interface GoogleHomeIntent {
    val context: JsonElement?
}

@Serializable
data class GoogleHomeDevice(val id: String, val customData: JsonElement? = null) {
    val deviceId = Integer.parseInt(id.removePrefix("scene-"))
}

@Serializable
data class GoogleHomeRes(val requestId: String, val payload: GoogleHomePayload)

interface GoogleHomeMayFail {
    val errorCode: String?
    val debugString: String?
}

@Serializable
sealed interface GoogleHomePayload : GoogleHomeMayFail {
    val requestId: String
}

@Serializable
data class AlleyDevice(val id: String, val type: String, val traits: List<String>, val name: AlleyDeviceNames, val willReportState: Boolean, val deviceInfo: AlleyDeviceInfo? = null, val attributes: Map<String, JsonElement>? = null, val customData: JsonElement? = null)

@Serializable
data class AlleyDeviceNames(val defaultNames: List<String>? = null, val name: String? = null, val nicknames: List<String>? = null)

@Serializable
data class AlleyDeviceInfo(val manufacturer: String, val model: String, val hwVersion: String, val swVersion: String)

@Serializable
@SerialName("action.devices.SYNC")
data class SyncIntent(
    override val context: JsonElement? = null
) : GoogleHomeIntent

@Serializable
@SerialName("action.devices.DISCONNECT")
data class DisconnectIntent(
    override val context: JsonElement? = null
) : GoogleHomeIntent

@Serializable
data class DisconnectResponse(
    override val requestId: String,
    override val errorCode: String? = null,
    override val debugString: String? = null
) : GoogleHomePayload

@Serializable
data class SyncResponse(override val requestId: String, val agentUserId: String? = null, override val errorCode: String? = null, override val debugString: String? = null, val devices: List<AlleyDevice>) : GoogleHomePayload

@Serializable
@SerialName("action.devices.QUERY")
data class QueryIntent(
    val payload: QueryIntentPayload,
    override val context: JsonElement? = null
) : GoogleHomeIntent

@Serializable
data class QueryIntentPayload(val devices: List<GoogleHomeDevice>)

@Serializable
data class QueryResponse(override val requestId: String, val devices: Map<String, DeviceState>, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomePayload

@Serializable
data class DeviceState(val online: Boolean, val on: Boolean? = null, val brightness: Int? = null, val color: DeviceColor? = null, val openState: List<DeviceBlindState>? = null)

@Serializable
data class DeviceBlindState(val openPercent: Int, val openDirection: DeviceBlindStateEnum)

enum class DeviceBlindStateEnum { UP, DOWN }

@Serializable
data class DeviceColor(val name: String? = null, val spectrumRGB: Int? = null, val temperature: Int? = null)

@Serializable
@SerialName("action.devices.EXECUTE")
data class ExecuteIntent(
    val payload: ExecuteIntentPayload,
    override val context: JsonElement? = null
) : GoogleHomeIntent

@Serializable
data class ExecuteIntentPayload(val commands: List<ExecuteIntentCommand>)

@Serializable
data class ExecuteIntentCommand(val devices: List<GoogleHomeDevice>, val execution: List<ExecuteIntentExecution>)

@Serializable
data class ExecuteIntentExecution(val command: String, val params: Map<String, JsonElement>)

@Serializable
data class ExecuteResponse(override val requestId: String, val commands: List<ExecuteResponseCommand>, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomePayload

@Serializable
data class ExecuteResponseCommand(val ids: List<String>, val status: ExecuteStatus, val states: DeviceState? = null, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomeMayFail

enum class ExecuteStatus {
    SUCCESS,
    PENDING,
    OFFLINE,
    ERROR
}
