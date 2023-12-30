@file:OptIn(ExperimentalSerializationApi::class)

package uk.co.thomasc.thealley.web.google

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.command.IGoogleHomeCommandBase

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
data class GoogleHomeRes(val requestId: String, val payload: JsonElement)

interface GoogleHomeMayFail {
    val errorCode: GoogleHomeErrorCode?
    val debugString: String?
}

@Serializable
sealed interface GoogleHomePayload : GoogleHomeMayFail {
    fun encodeToJson(json: Json): JsonElement
}

@Serializable
data class AlleyDevice(
    val id: String,
    val type: DeviceType,
    val traits: Set<String>,
    val name: AlleyDeviceNames,
    val willReportState: Boolean,
    val deviceInfo: AlleyDeviceInfo? = null,
    val attributes: Map<String, JsonElement>? = null,
    val customData: JsonElement? = null
)

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
    override val errorCode: GoogleHomeErrorCode? = null,
    override val debugString: String? = null
) : GoogleHomePayload {
    override fun encodeToJson(json: Json) = alleyJson.encodeToJsonElement(this)
}

@Serializable
data class SyncResponse(
    val agentUserId: String,
    override val errorCode: GoogleHomeErrorCode? = null,
    override val debugString: String? = null,
    val devices: List<AlleyDevice>
) : GoogleHomePayload {
    override fun encodeToJson(json: Json) = alleyJson.encodeToJsonElement(this)
}

@Serializable
@SerialName("action.devices.QUERY")
data class QueryIntent(
    val payload: QueryIntentPayload,
    override val context: JsonElement? = null
) : GoogleHomeIntent

@Serializable
data class QueryIntentPayload(val devices: List<GoogleHomeDevice>)

@Serializable
data class QueryResponse(
    val devices: Map<String, JsonObject>,
    override val errorCode: GoogleHomeErrorCode? = null,
    override val debugString: String? = null
) : GoogleHomePayload {
    override fun encodeToJson(json: Json) = alleyJson.encodeToJsonElement(this)
}

@Serializable
data class DeviceBlindState(val openPercent: Int, val openDirection: DeviceBlindStateEnum)

enum class DeviceBlindStateEnum { UP, DOWN }

interface DeviceColor {
    val name: String?
    val spectrumRgb: Int?
    val spectrumHsv: DeviceColorHSV?
    val temperature: Int?
}

@Serializable
data class DeviceColorState(
    override val name: String? = null,
    override val spectrumRgb: Int? = null,
    override val spectrumHsv: DeviceColorHSV? = null,
    @SerialName("temperatureK")
    override val temperature: Int? = null
) : DeviceColor

@Serializable
data class DeviceColorCommand(
    override val name: String? = null,
    @SerialName("spectrumRGB")
    override val spectrumRgb: Int? = null,
    @SerialName("spectrumHSV")
    override val spectrumHsv: DeviceColorHSV? = null,
    override val temperature: Int? = null
) : DeviceColor

@Serializable
data class DeviceColorHSV(
    val hue: Float,
    val saturation: Float,
    val value: Float
)

@Serializable
@SerialName("action.devices.EXECUTE")
data class ExecuteIntent(
    val payload: ExecuteIntentPayload,
    override val context: JsonElement? = null
) : GoogleHomeIntent

@Serializable
data class ExecuteIntentPayload(val commands: List<ExecuteIntentCommand>)

@Serializable
data class ExecuteIntentCommand(val devices: List<GoogleHomeDevice>, val execution: List<IGoogleHomeCommandBase>)

@Serializable
data class ExecuteResponse(
    val commands: List<ExecuteResponseCommand>,
    override val errorCode: GoogleHomeErrorCode? = null,
    override val debugString: String? = null
) : GoogleHomePayload {
    override fun encodeToJson(json: Json) = alleyJson.encodeToJsonElement(this)
}

@Serializable
data class ExecuteResponseCommand(
    val ids: List<String>,
    val status: String,
    val states: JsonObject? = null,
    override val errorCode: GoogleHomeErrorCode? = null,
    override val debugString: String? = null
) : GoogleHomeMayFail

enum class QueryStatus {
    SUCCESS, OFFLINE, EXCEPTIONS, ERROR
}
