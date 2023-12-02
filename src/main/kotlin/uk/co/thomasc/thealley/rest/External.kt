package uk.co.thomasc.thealley.rest

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.checkOauth
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.config.AlleyTokenStore
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IAlleyLight
import uk.co.thomasc.thealley.devicev2.kasa.bulb.BulbDevice
import uk.co.thomasc.thealley.devicev2.relay.RelayDevice
import uk.co.thomasc.thealley.devicev2.system.scene.SceneDevice
import uk.co.thomasc.thealley.devicev2.xiaomi.blind.BlindDevice
import java.awt.Color

@Location("/external")
class ExternalRoute {
    @Location("/googlehome")
    data class GoogleHome(val api: ExternalRoute)

    @Location("/test")
    data class Test(val api: ExternalRoute)
}

val threadPool = newFixedThreadPoolContext(10, "ExternalRoute")

object ExternalLogger : KLogging()

fun Route.externalRoute(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper, alleyTokenStore: AlleyTokenStore) {
    val logger = ExternalLogger.logger

    suspend fun executeRequest(requestId: String, intent: ExecuteIntent) = ExecuteResponse(
        requestId,
        intent.payload.commands.map { cmd -> // Fetch Devices
            cmd to cmd.devices
        }.map { // Execute commands
            coroutineScope {
                it.second.map { device ->
                    async(threadPool) {
                        device to it.first.execution.map { ex ->
                            val bulbN = deviceMapper.getDevice(device.deviceId) as? IAlleyLight
                            when (ex.command) {
                                "action.devices.commands.ActivateScene" -> {
                                    deviceMapper.getDevice<SceneDevice>(device.deviceId)?.let {
                                        if (ex.params["deactivate"]?.jsonPrimitive?.booleanOrNull != true) {
                                            it.off(bus)
                                        } else {
                                            it.execute(bus)
                                        }
                                    }

                                    ExecuteStatus.SUCCESS
                                }
                                "action.devices.commands.OnOff" -> bulbN?.let {
                                    it.setPowerState(bus, ex.params["on"]?.jsonPrimitive?.booleanOrNull ?: false)

                                    ExecuteStatus.SUCCESS
                                }
                                "action.devices.commands.BrightnessAbsolute" -> bulbN?.let {
                                    bulbN.setComplexState(bus, IAlleyLight.LightState(ex.params["brightness"]?.jsonPrimitive?.intOrNull))

                                    ExecuteStatus.SUCCESS
                                }
                                "action.devices.commands.OpenClose" -> bulbN?.let {
                                    bulbN.setComplexState(bus, IAlleyLight.LightState(ex.params["openPercent"]?.jsonPrimitive?.intOrNull))

                                    ExecuteStatus.SUCCESS
                                }
                                "action.devices.commands.ColorAbsolute" -> bulbN?.let {
                                    val colorObj = ex.params["color"]

                                    if (colorObj is JsonObject) {
                                        if (colorObj.containsKey("temperature")) {
                                            bulbN.setComplexState(
                                                bus,
                                                IAlleyLight.LightState(temperature = colorObj["temperature"]?.jsonPrimitive?.intOrNull)
                                            )
                                        } else {
                                            val colorHex = colorObj["spectrumRGB"]?.jsonPrimitive?.intOrNull ?: 0
                                            val color = Color.RGBtoHSB(
                                                (colorHex shr 16) and 255,
                                                (colorHex shr 8) and 255,
                                                colorHex and 255,
                                                null
                                            )

                                            bulbN.setComplexState(
                                                bus,
                                                IAlleyLight.LightState(
                                                    (color[2] * 100).toInt(),
                                                    (color[0] * 360).toInt(),
                                                    (color[1] * 100).toInt()
                                                )
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
        }.flatMap { cmd -> // Collate results
            // Commands -> Devices -> Executions
            cmd.map { localDevices ->
                val devices = localDevices.await()

                devices.first to devices.second.fold(ExecuteStatus.SUCCESS) { acc, v ->
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

    suspend fun queryRequest(requestId: String, intent: QueryIntent) = QueryResponse(
        requestId,
        intent.payload.devices.mapNotNull {
            deviceMapper.getDevice(it.deviceId)
        }.associate { light ->

            suspend fun getState() = when (light) {
                is BulbDevice -> light.getLightState().let { lightState ->
                    DeviceState(
                        true,
                        light.getPowerState(),
                        lightState.brightness,
                        if (lightState.temperature != null && lightState.temperature > 0) {
                            DeviceColor(
                                temperature = lightState.temperature
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
                is RelayDevice -> DeviceState(true, light.getPowerState())
                is BlindDevice -> DeviceState(
                    true,
                    openState = light.getLightState().brightness?.let { s ->
                        listOf(DeviceBlindState(s, if (s > 0) DeviceBlindStateEnum.UP else DeviceBlindStateEnum.DOWN))
                    }
                )
                else -> null
            } ?: DeviceState(false)

            light.id.toString() to getState()
        }
    )

    suspend fun syncRequest(requestId: String, userId: String, intent: SyncIntent) = SyncResponse(
        requestId,
        userId,
        devices = deviceMapper.getDevices<BulbDevice>().map { light ->

            AlleyDevice(
                light.id.toString(),
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
                    name = light.config.name
                ),
                false,
                attributes = mapOf(
                    "temperatureMinK" to JsonPrimitive(2500),
                    "temperatureMaxK" to JsonPrimitive(9000)
                ),
                deviceInfo = AlleyDeviceInfo(
                    "TP-Link",
                    light.getModel() ?: "",
                    light.getHwVer() ?: "",
                    light.getSwVer() ?: ""
                )
            )
        } + deviceMapper.getDevices<RelayDevice>().map {
            AlleyDevice(
                it.id.toString(),
                "action.devices.types.LIGHT",
                listOf(
                    "action.devices.traits.OnOff"
                ),
                AlleyDeviceNames(
                    name = it.config.name
                ),
                false
            )
        } + deviceMapper.getDevices<BlindDevice>().map {
            AlleyDevice(
                it.id.toString(),
                "action.devices.types.BLINDS",
                listOf(
                    "action.devices.traits.OpenClose"
                ),
                AlleyDeviceNames(
                    name = it.config.name
                ),
                false,
                attributes = mapOf(
                    "openDirection" to JsonArray(DeviceBlindStateEnum.entries.map { e -> JsonPrimitive(e.toString()) })
                )
            )
        } + deviceMapper.getDevices<SceneDevice>().map {
            AlleyDevice(
                "scene-${it.id}",
                "action.devices.types.SCENE",
                listOf(
                    "action.devices.traits.Scene"
                ),
                AlleyDeviceNames(
                    name = "scene-${it.id}"
                ),
                false,
                attributes = mapOf(
                    "sceneReversible" to JsonPrimitive(true)
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
        checkOauth(alleyTokenStore) { userId ->
            val txt = call.receiveText()
            logger.info { "Received google home request - $txt" }
            // val obj = call.receive<GoogleHomeReq>()
            val obj = alleyJson.decodeFromString<GoogleHomeReq>(txt)
            val intent = obj.inputs.first()

            when (intent) {
                is SyncIntent -> syncRequest(obj.requestId, userId, intent)
                is QueryIntent -> queryRequest(obj.requestId, intent)
                is ExecuteIntent -> executeRequest(obj.requestId, intent)
                is DisconnectIntent -> DisconnectResponse(obj.requestId)
            }.let {
                val txt2 = alleyJson.encodeToString(it)
                logger.info { "Responding - $txt2" }
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
