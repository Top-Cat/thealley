package uk.co.thomasc.thealley.rest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.devices.Relay
import uk.co.thomasc.thealley.repo.SwitchRepository
import java.awt.Color

@RestController
@RequestMapping("/external")
class External(
    val switchRepository: SwitchRepository,
    relayClient: RelayClient,
    val deviceMapper: DeviceMapper
) {

    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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
            is SyncIntent -> syncRequest(intent)
            is QueryIntent -> queryRequest(intent)
            is ExecuteIntent -> executeRequest(intent)
            else -> null
        }?.let {
            GoogleHomeRes(
                obj.requestId,
                it
            )
        }
        //[{"intent":"action.devices.SYNC"}]
        //println(obj.inputs)
    }

    fun executeRequest(intent: ExecuteIntent) = ExecuteResponse(
        intent.payload.commands.map { cmd -> // Fetch Devices
            cmd to cmd.devices.map {
                switchRepository.getDeviceForId(it.deviceId)
            }.map {
                it.deviceId to deviceMapper.toLight(it)
            }
        }.map { // Execute commands
            it.second.map {
                devices ->

                async(CommonPool) {
                    devices.first to it.first.execution.map { ex ->
                        val dev = devices.second

                        dev?.let { bulbN ->
                            when (ex.command) {
                                "action.devices.commands.OnOff" -> {
                                        bulbN.setPowerState(ex.params["on"] as Boolean)

                                        ExecuteStatus.SUCCESS
                                }
                                "action.devices.commands.BrightnessAbsolute" -> {
                                        bulbN.setComplexState(ex.params["brightness"] as Int)

                                        ExecuteStatus.SUCCESS
                                }
                                "action.devices.commands.ColorAbsolute" -> {
                                    val colorObj = ex.params["color"]

                                    if (colorObj is Map<*, *>) {
                                        if (colorObj.containsKey("temperature")) {
                                            bulbN.setComplexState(
                                                temperature = colorObj["temperature"] as Int
                                            )
                                        } else {
                                            val colorHex = colorObj["spectrumRGB"] as Int
                                            val color = Color.RGBtoHSB(
                                                (colorHex shr 16) and 255,
                                                (colorHex shr 8) and 255,
                                                colorHex and 255,
                                                null
                                            )

                                            bulbN.setComplexState(
                                                (color[2] * 100).toInt(),
                                                (color[0] * 360).toInt(),
                                                (color[1] * 100).toInt()
                                            )
                                        }

                                        // TODO: Check values were set?

                                        ExecuteStatus.SUCCESS
                                    } else {
                                        ExecuteStatus.ERROR
                                    }
                                }
                                else -> ExecuteStatus.ERROR
                            }
                        }
                    }
                }
            }
        }.flatMap { // Collate results
            // Commands -> Devices -> Executions
            cmd ->
            cmd.map {
                _devices ->

                val devices = runBlocking { _devices.await() }

                devices.first to devices.second.fold(ExecuteStatus.SUCCESS) {
                    acc, v ->

                    if (v == ExecuteStatus.OFFLINE || acc == ExecuteStatus.OFFLINE) {
                        ExecuteStatus.OFFLINE
                    } else if (v == ExecuteStatus.ERROR) {
                        ExecuteStatus.ERROR
                    } else {
                        acc
                    }
                }
            }
        }.groupBy({ it.second }, { it.first }).map {
            ExecuteResponseCommand(
                it.value.map { it.toString() },
                it.key
            )
        }
    )

    fun queryRequest(intent: QueryIntent) = QueryResponse(
        intent.payload.devices.map {
            switchRepository.getDeviceForId(it.deviceId)
        }.map {
            deviceMapper.toLight(it) to it
        }.map {
            val light = it.first
            val dbInfo = it.second

            dbInfo.deviceId.toString() to (when (light) {
                is Bulb -> light.getLightState()?.let { lightState ->
                    DeviceState(
                        true,
                        light.getPowerState(),
                        lightState.brightness,
                        if (lightState.color_temp != null && lightState.color_temp > 0) {
                            DeviceColor(
                                temperature = lightState.color_temp
                            )
                        } else {
                            DeviceColor(
                                spectrumRGB = Color.HSBtoRGB(
                                    (lightState.hue ?: 0) / 360f,
                                    (lightState.saturation ?: 0) / 100f,
                                    (lightState.brightness ?: 0) / 100f
                                )
                            )
                        }
                    )
                }
                is Relay -> DeviceState(true, light.getPowerState())
                else -> null
            } ?: DeviceState(false))
        }.toMap()
    )

    fun syncRequest(intent: SyncIntent) = SyncResponse(
        devices = switchRepository.getDevicesForType(SwitchRepository.DeviceType.BULB).map {
            deviceMapper.toLight(it) to it
        }.map { mapIn ->
            val light = mapIn.first as Bulb
            val dbInfo = mapIn.second

            AlleyDevice(
                dbInfo.deviceId.toString(),
                "action.devices.types.LIGHT",
                listOf(
                    "action.devices.traits.OnOff",
                    "action.devices.traits.Brightness",
                    "action.devices.traits.ColorTemperature",
                    "action.devices.traits.ColorSpectrum"
                ),
                AlleyDeviceNames(
                    defaultNames = listOf(
                        light.getModel()
                    ),
                    name = dbInfo.name
                ),
                false,
                attributes = mapOf(
                    "temperatureMinK" to 2500,
                    "temperatureMaxK" to 9000
                ),
                deviceInfo = AlleyDeviceInfo(
                    "TP-Link",
                    light.getModel(),
                    light.getHwVer(),
                    light.getSwVer()
                )
            )
        } + switchRepository.getDevicesForType(SwitchRepository.DeviceType.RELAY).map {
            AlleyDevice(
                it.deviceId.toString(),
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

}
