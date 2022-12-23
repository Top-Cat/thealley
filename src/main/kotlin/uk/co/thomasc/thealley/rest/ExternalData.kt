package uk.co.thomasc.thealley.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import uk.co.thomasc.thealley.devices.DeviceMapper

data class GoogleHomeReq(val requestId: String, val inputs: List<JsonNode>)
data class GoogleHomeDevice(val id: String, val customData: JsonNode?) : DeviceMapper.HasDeviceId {
    override val deviceId = Integer.parseInt(id.removePrefix("scene-"))
}
data class GoogleHomeRes(val requestId: String, val payload: GoogleHomePayload)
abstract class GoogleHomeMayFail(open val errorCode: String?, open val debugString: String?)
sealed class GoogleHomePayload(errorCode: String?, debugString: String?) : GoogleHomeMayFail(errorCode, debugString)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AlleyDevice(val id: String, val type: String, val traits: List<String>, val name: AlleyDeviceNames, val willReportState: Boolean, val deviceInfo: AlleyDeviceInfo? = null, val attributes: Map<String, Any>? = null, val customData: Any? = null)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AlleyDeviceNames(val defaultNames: List<String>? = null, val name: String? = null, val nicknames: List<String>? = null)

data class AlleyDeviceInfo(val manufacturer: String, val model: String, val hwVersion: String, val swVersion: String)

data class SyncIntent(val intent: String)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SyncResponse(val agentUserId: String? = null, override val errorCode: String? = null, override val debugString: String? = null, val devices: List<AlleyDevice>) : GoogleHomePayload(errorCode, debugString)

data class QueryIntent(val intent: String, val payload: QueryIntentPayload)
data class QueryIntentPayload(val devices: List<GoogleHomeDevice>)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class QueryResponse(val devices: Map<String, DeviceState>, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomePayload(errorCode, debugString)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DeviceState(val online: Boolean, val on: Boolean? = null, val brightness: Int? = null, val color: DeviceColor? = null, val openState: List<DeviceBlindState>? = null)
data class DeviceBlindState(val openPercent: Int, val openDirection: DeviceBlindStateEnum)
enum class DeviceBlindStateEnum { UP, DOWN }
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DeviceColor(val name: String? = null, val spectrumRGB: Int? = null, val temperature: Int? = null)

data class ExecuteIntent(val intent: String, val payload: ExecuteIntentPayload)
data class ExecuteIntentPayload(val commands: List<ExecuteIntentCommand>)
data class ExecuteIntentCommand(val devices: List<GoogleHomeDevice>, val execution: List<ExecuteIntentExecution>)
data class ExecuteIntentExecution(val command: String, val params: Map<String, Any>)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExecuteResponse(val commands: List<ExecuteResponseCommand>, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomePayload(errorCode, debugString)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExecuteResponseCommand(val ids: List<String>, val status: ExecuteStatus, val states: DeviceState? = null, override val errorCode: String? = null, override val debugString: String? = null) : GoogleHomeMayFail(errorCode, debugString)
enum class ExecuteStatus {
    SUCCESS,
    PENDING,
    OFFLINE,
    ERROR
}
