package uk.co.thomasc.thealley.rest

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import uk.co.thomasc.thealley.devices.DeviceMapper

@Serializable
data class GoogleHomeReq(val requestId: String, val inputs: List<JsonElement>)
@Serializable
data class GoogleHomeDevice(val id: String, val customData: JsonElement) : DeviceMapper.HasDeviceId {
    override val deviceId = Integer.parseInt(id.removePrefix("scene-"))
}
@Serializable
data class GoogleHomeRes(val requestId: String, val payload: GoogleHomePayload)
interface GoogleHomeMayFail {
    val errorCode: String?
    val debugString: String?
}
@Serializable
sealed class GoogleHomePayload : GoogleHomeMayFail
@Serializable
data class AlleyDevice(val id: String, val type: String, val traits: List<String>, val name: AlleyDeviceNames, val willReportState: Boolean, val deviceInfo: AlleyDeviceInfo? = null, val attributes: Map<String, JsonElement>? = null, val customData: JsonElement? = null)
@Serializable
data class AlleyDeviceNames(val defaultNames: List<String>? = null, val name: String? = null, val nicknames: List<String>? = null)
@Serializable
data class AlleyDeviceInfo(val manufacturer: String, val model: String, val hwVersion: String, val swVersion: String)
@Serializable
data class SyncIntent(val intent: String)
@Serializable
data class SyncResponse(val agentUserId: String? = null, override val errorCode: String? = null, override val debugString: String? = null, val devices: List<AlleyDevice>) : GoogleHomePayload()

@Serializable
data class QueryIntent(val intent: String, val payload: QueryIntentPayload)
@Serializable
data class QueryIntentPayload(val devices: List<GoogleHomeDevice>)
@Serializable
data class QueryResponse(val devices: Map<String, DeviceState>, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomePayload()
@Serializable
data class DeviceState(val online: Boolean, val on: Boolean? = null, val brightness: Int? = null, val color: DeviceColor? = null, val openState: List<DeviceBlindState>? = null)
@Serializable
data class DeviceBlindState(val openPercent: Int, val openDirection: DeviceBlindStateEnum)
enum class DeviceBlindStateEnum { UP, DOWN }
@Serializable
data class DeviceColor(val name: String? = null, val spectrumRGB: Int? = null, val temperature: Int? = null)
@Serializable
data class ExecuteIntent(val intent: String, val payload: ExecuteIntentPayload)
@Serializable
data class ExecuteIntentPayload(val commands: List<ExecuteIntentCommand>)
@Serializable
data class ExecuteIntentCommand(val devices: List<GoogleHomeDevice>, val execution: List<ExecuteIntentExecution>)
@Serializable
data class ExecuteIntentExecution(val command: String, val params: Map<String, JsonElement>)
@Serializable
data class ExecuteResponse(val commands: List<ExecuteResponseCommand>, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomePayload()
@Serializable
data class ExecuteResponseCommand(val ids: List<String>, val status: ExecuteStatus, val states: DeviceState? = null, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomeMayFail
enum class ExecuteStatus {
    SUCCESS,
    PENDING,
    OFFLINE,
    ERROR
}
