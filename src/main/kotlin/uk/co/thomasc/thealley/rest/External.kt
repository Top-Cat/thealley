package uk.co.thomasc.thealley.rest

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import uk.co.thomasc.thealley.checkOauth
import uk.co.thomasc.thealley.client.jackson
import uk.co.thomasc.thealley.config.AlleyTokenStore
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.devices.Relay
import uk.co.thomasc.thealley.repo.SwitchRepository
import uk.co.thomasc.thealley.scenes.SceneController
import java.awt.Color

@Location("/external")
class ExternalRoute {
    @Location("/googlehome")
    data class GoogleHome(val api: ExternalRoute)
    @Location("/test")
    data class Test(val api: ExternalRoute)
}

val threadPool = newFixedThreadPoolContext(10, "ExternalRoute")

fun Route.externalRoute(switchRepository: SwitchRepository, sceneController: SceneController, alleyTokenStore: AlleyTokenStore, deviceMapper: DeviceMapper) {
    fun executeRequest(intent: ExecuteIntent) = ExecuteResponse(
        intent.payload.commands.map { cmd -> // Fetch Devices
            cmd to cmd.devices.map {
                it to switchRepository.getDeviceForId(it.deviceId)
            }.map {
                it.first to deviceMapper.toLight(it.second)
            }
        }.map { // Execute commands
            runBlocking {
                it.second.map { devices ->
                    async(threadPool) {
                        devices.first to it.first.execution.map { ex ->
                            val dev = devices.second

                            dev?.let { bulbN ->
                                when (ex.command) {
                                    "action.devices.commands.ActivateScene" -> {
                                        sceneController.scenes[devices.first.deviceId]?.let {
                                            if (ex.params["deactivate"] as Boolean) {
                                                it.off()
                                            } else {
                                                it.execute()
                                            }
                                        }

                                        ExecuteStatus.SUCCESS
                                    }
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
                it.value.map { device -> device.id },
                it.key
            )
        }
    )

    suspend fun queryRequest(intent: QueryIntent) = QueryResponse(
        intent.payload.devices.map {
            switchRepository.getDeviceForId(it.deviceId)
        }.map {
            deviceMapper.toLight(it) to it
        }.associate {
            val light = it.first
            val dbInfo = it.second

            suspend fun getState() = when (light) {
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
            } ?: DeviceState(false)

            dbInfo.deviceId.toString() to getState()
        }
    )

    suspend fun syncRequest(intent: SyncIntent) = SyncResponse(
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
                    defaultNames = listOfNotNull(
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
                    light.getModel() ?: "",
                    light.getHwVer() ?: "",
                    light.getSwVer() ?: ""
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
        } + sceneController.scenes.map {
            AlleyDevice(
                "scene-${it.key}",
                "action.devices.types.SCENE",
                listOf(
                    "action.devices.traits.Scene"
                ),
                AlleyDeviceNames(
                    name = "scene-${it.key}"
                ),
                false,
                attributes = mapOf(
                    "sceneReversible" to true
                )
            )
        }
    )

    get<ExternalRoute.Test> {
        checkOauth(alleyTokenStore) {
            call.respond("Hi")
        }
    }

    post<ExternalRoute.GoogleHome> {
        checkOauth(alleyTokenStore) {
            val obj = call.receive<GoogleHomeReq>()
            val input = obj.inputs.first()

            val intent = jackson.treeToValue(
                input,
                when (input.get("intent").textValue()) {
                    "action.devices.QUERY" -> QueryIntent::class
                    "action.devices.EXECUTE" -> ExecuteIntent::class
                    else -> SyncIntent::class
                }.java
            )

            when (intent) {
                is SyncIntent -> syncRequest(intent)
                is QueryIntent -> queryRequest(intent)
                is ExecuteIntent -> executeRequest(intent)
                else -> null
            }?.let {
                call.respond(
                    GoogleHomeRes(
                        obj.requestId,
                        it
                    )
                )
            }
        }
    }
}
