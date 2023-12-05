package uk.co.thomasc.thealley.rest

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.checkOauth
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.config.AlleyTokenStore
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.google.command.ActivateSceneCommand
import uk.co.thomasc.thealley.google.command.ColorAbsoluteCommand
import uk.co.thomasc.thealley.google.command.IBrightnessCommand
import uk.co.thomasc.thealley.google.command.OnOffCommand
import uk.co.thomasc.thealley.google.command.OpenCloseCommand
import uk.co.thomasc.thealley.google.trait.BrightnessTrait
import uk.co.thomasc.thealley.google.trait.ColorSettingTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait
import uk.co.thomasc.thealley.google.trait.SceneTrait

@Location("/external")
class ExternalRoute {
    @Location("/googlehome")
    data class GoogleHome(val api: ExternalRoute)

    @Location("/test")
    data class Test(val api: ExternalRoute)
}

val threadPool = newFixedThreadPoolContext(10, "ExternalRoute")

class ExternalHandler(private val deviceMapper: AlleyDeviceMapper) {
    private suspend fun executeRequest(intent: ExecuteIntent) = ExecuteResponse(
        intent.payload.commands.map { cmd -> // Fetch Devices
            cmd to cmd.devices
        }.map { // Execute commands
            coroutineScope {
                it.second.map { device ->
                    async(threadPool) {
                        device to it.first.execution.map { ex ->
                            val dev = deviceMapper.getDevice(device.deviceId)
                            val type = dev?.ghType
                            val traits = dev?.ghTraits

                            if (type != null && traits != null) {
                                traits.firstNotNullOfOrNull { trait ->
                                    when {
                                        trait is BrightnessTrait && ex is IBrightnessCommand<*> -> trait.handleCommand(ex)
                                        trait is ColorSettingTrait && ex is ColorAbsoluteCommand -> trait.handleCommand(ex)
                                        trait is OnOffTrait && ex is OnOffCommand -> trait.handleCommand(ex)
                                        trait is OpenCloseTrait && ex is OpenCloseCommand -> trait.handleCommand(ex)
                                        trait is SceneTrait && ex is ActivateSceneCommand -> trait.handleCommand(ex)
                                        else -> null
                                    }
                                }
                            } else {
                                null
                            } ?: ExecuteStatus.ERROR
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

    private suspend fun queryRequest(intent: QueryIntent) = QueryResponse(
        intent.payload.devices.mapNotNull {
            deviceMapper.getDevice(it.deviceId)
        }.associate { light ->
            val type = light.ghType
            val traits = light.ghTraits

            val state = if (type != null && traits != null) {
                traits.fold(
                    mapOf<String, JsonElement>(
                        "online" to JsonPrimitive(true),
                        "status" to JsonPrimitive(QueryStatus.SUCCESS.name)
                    )
                ) { a, b ->
                    a.plus(b.getState())
                }
            } else {
                mapOf(
                    "online" to JsonPrimitive(false),
                    "status" to JsonPrimitive(QueryStatus.ERROR.name),
                    "errorCode" to JsonPrimitive("deviceOffline")
                )
            }

            light.id.toString() to JsonObject(state)
        }
    )

    private suspend fun syncRequest(userId: String, intent: SyncIntent) = SyncResponse(
        userId,
        devices = deviceMapper.getDevices().mapNotNull {
            val type = it.ghType
            val traits = it.ghTraits

            if (type != null && traits != null) {
                AlleyDevice(
                    it.id.toString(),
                    type.typeName,
                    traits.map { t -> t.name }.toSet(),
                    AlleyDeviceNames(name = it.config.name),
                    false,
                    attributes = traits.fold(mapOf()) { a, b ->
                        a.plus(b.getAttributes())
                    }
                )
            } else {
                null
            }
        }
    )

    suspend fun handleRequest(userId: String, req: GoogleHomeReq) =
        GoogleHomeRes(
            req.requestId,
            when (val intent = req.inputs.first()) {
                is SyncIntent -> syncRequest(userId, intent)
                is QueryIntent -> queryRequest(intent)
                is ExecuteIntent -> executeRequest(intent)
                is DisconnectIntent -> DisconnectResponse()
            }.encodeToJson(alleyJson)
        )
}

fun Route.externalRoute(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper, alleyTokenStore: AlleyTokenStore) {
    get<ExternalRoute.Test> {
        checkOauth(alleyTokenStore) {
            call.respond("Hi")
        }
    }

    val externalHandler = ExternalHandler(deviceMapper)
    post<ExternalRoute.GoogleHome> {
        checkOauth(alleyTokenStore) { userId ->
            val obj = call.receive<GoogleHomeReq>()
            call.respond(externalHandler.handleRequest(userId, obj))
        }
    }
}
