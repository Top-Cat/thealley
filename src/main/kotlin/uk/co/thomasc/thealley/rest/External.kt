package uk.co.thomasc.thealley.rest

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.repo.SwitchRepository

data class GoogleHomeReq(val requestId: String, val inputs: List<JsonNode>)
data class GoogleHomeDevice(val id: String, val customData: JsonNode)
data class GoogleHomeRes(val requestId: String, val payload: Any)

@JsonInclude(Include.NON_NULL)
data class AlleyDevice(val id: String, val type: String, val traits: List<String>, val name: AlleyDeviceNames, val willReportState: Boolean, val deviceInfo: AlleyDeviceInfo? = null, val attributes: Map<String, Any>? = null, val customData: Any? = null)
@JsonInclude(Include.NON_NULL)
data class AlleyDeviceNames(val defaultNames: List<String>? = null, val name: String? = null, val nicknames: List<String>? = null)
data class AlleyDeviceInfo(val manufacturer: String, val model: String, val hwVersion: String, val swVersion: String)

data class SyncIntent(val intent: String)
@JsonInclude(Include.NON_NULL)
data class SyncResponse(val agentUserId: String? = null, val errorCode: String? = null, val debugString: String? = null, val devices: List<AlleyDevice>)

data class QueryIntent(val intent: String, val payload: QueryIntentPayload)
data class QueryIntentPayload(val devices: List<GoogleHomeDevice>)

data class ExecuteIntent(val intent: String, val payload: ExecuteIntentPayload)
data class ExecuteIntentPayload(val commands: List<ExecuteIntentCommand>)
data class ExecuteIntentCommand(val devices: List<GoogleHomeDevice>, val execution: List<ExecuteIntentExecution>)
data class ExecuteIntentExecution(val command: String, val params: Map<String, Any>)

@RestController
@RequestMapping("/external")
class External(val switchRepository: SwitchRepository) {

    val mapper = jacksonObjectMapper()

    @PostMapping("/googlehome")
    fun googleHomeReq(@RequestBody obj: GoogleHomeReq): Any? {
        // Works if you want to populate response user id
        //val auth = SecurityContextHolder.getContext().authentication.principal as AlleyUser

        val input = obj.inputs.first()

        val intent = mapper.treeToValue(input,
            when (input.get("intent").textValue()) {
                "action.devices.QUERY" -> QueryIntent::class
                "action.devices.EXECUTE" -> ExecuteIntent::class
                else -> SyncIntent::class
            }.java
        )

        return when (intent) {
            is SyncIntent -> {
                GoogleHomeRes(
                    obj.requestId,
                    SyncResponse(
                        devices = switchRepository.getDevicesForType(SwitchRepository.DeviceType.BULB).map {
                            AlleyDevice(
                                it.id.toString(),
                                "action.devices.types.LIGHT",
                                listOf(
                                    "action.devices.traits.OnOff",
                                    "action.devices.traits.Brightness",
                                    "action.devices.traits.ColorTemperature",
                                    "action.devices.traits.ColorSpectrum"
                                ),
                                AlleyDeviceNames(
                                    name = it.name
                                ),
                                false,
                                attributes = mapOf(
                                    "temperatureMinK" to 2500,
                                    "temperatureMaxK" to 9000
                                )
                            )
                        } + switchRepository.getDevicesForType(SwitchRepository.DeviceType.RELAY).map {
                            AlleyDevice(
                                it.id.toString(),
                                "action.devices.types.LIGHT",
                                listOf(
                                    "action.devices.traits.OnOff"
                                ),
                                AlleyDeviceNames(
                                    name = it.name
                                ),
                                false
                            )
                        }
                    )
                )
            }
            else -> null
        }
        //[{"intent":"action.devices.SYNC"}]
        //println(obj.inputs)
    }

}
