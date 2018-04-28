package uk.co.thomasc.thealley.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.client.DeviceResponse
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.Relay
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.devices.BulbData
import uk.co.thomasc.thealley.repo.SwitchRepository
import java.awt.Color

@RestController
@RequestMapping("/external")
class External(val switchRepository: SwitchRepository, val relayClient: RelayClient, val localClient: LocalClient) {

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
                switchRepository.getDeviceForId(Integer.parseInt(it.id))
            }.map {
                it.id to when (it.type) {
                    SwitchRepository.DeviceType.BULB -> localClient.getDevice(it.hostname)
                    SwitchRepository.DeviceType.RELAY -> relayClient.getRelay(it.hostname)
                    else -> null
                }
            }
        }.map { // Execute commands
            it.second.map {
                devices ->

                async(CommonPool) {
                    devices.first to it.first.execution.map { ex ->
                        val dev = devices.second

                        when (dev) {
                            is Relay -> dev
                            is DeviceResponse -> dev.bulb { it }.await()
                            else -> null
                        }?.let { bulbN ->
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

                                    // Turns light on with transition in last state,
                                    // otherwise the call below will only go to the last state
                                    bulbN.setComplexState(transitionTime = 30000)

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
            switchRepository.getDeviceForId(Integer.parseInt(it.id))
        }.map {
            async(CommonPool) {
                localClient.getSysInfo(it.hostname, timeout = 2000)
            } to it
        }.map {
            val sysInfo = runBlocking {
                it.first.await()
            } as BulbData?

            val dbInfo = it.second

            dbInfo.id.toString() to when (sysInfo) {
                null -> DeviceState(false)
                else -> DeviceState(
                    true,
                    sysInfo.light_state.on_off,
                    sysInfo.light_state.brightness,
                    if (sysInfo.light_state.color_temp != null && sysInfo.light_state.color_temp > 0) {
                        DeviceColor(
                            temperature = sysInfo.light_state.color_temp
                        )
                    } else {
                        DeviceColor(
                            spectrumRGB = Color.HSBtoRGB(
                                (sysInfo.light_state.hue ?: 0) / 360f,
                                (sysInfo.light_state.saturation ?: 0) / 100f,
                                (sysInfo.light_state.brightness ?: 0) / 100f
                            )
                        )
                    }
                )
            }
        }.toMap()
    )

    fun syncRequest(intent: SyncIntent) = SyncResponse(
        devices = switchRepository.getDevicesForType(SwitchRepository.DeviceType.BULB).map {
            async(CommonPool) {
                localClient.getSysInfo(it.hostname, timeout = 2000)
            } to it
        }.map { mapIn ->

            val sysInfo = runBlocking {
                mapIn.first.await()
            } as BulbData

            val dbInfo = mapIn.second

            AlleyDevice(
                dbInfo.id.toString(),
                "action.devices.types.LIGHT",
                listOf(
                    "action.devices.traits.OnOff",
                    "action.devices.traits.Brightness",
                    "action.devices.traits.ColorTemperature",
                    "action.devices.traits.ColorSpectrum"
                ),
                AlleyDeviceNames(
                    defaultNames = listOf(
                        sysInfo.model
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
                    sysInfo.model,
                    sysInfo.hw_ver,
                    sysInfo.sw_ver
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

}
