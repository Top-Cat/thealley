package uk.co.thomasc.thealley.web

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.client
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.ReportStateEvent
import uk.co.thomasc.thealley.google.command.IGoogleHomeFollowUpCommand
import uk.co.thomasc.thealley.google.followup.FollowUpAuth
import uk.co.thomasc.thealley.google.followup.FollowUpDevices
import uk.co.thomasc.thealley.google.followup.FollowUpNotification
import uk.co.thomasc.thealley.google.followup.FollowUpPayload
import uk.co.thomasc.thealley.google.followup.FollowUpResponse
import uk.co.thomasc.thealley.google.followup.IFollowUp
import uk.co.thomasc.thealley.web.google.AlleyDevice
import uk.co.thomasc.thealley.web.google.AlleyDeviceNames
import uk.co.thomasc.thealley.web.google.DisconnectIntent
import uk.co.thomasc.thealley.web.google.DisconnectResponse
import uk.co.thomasc.thealley.web.google.ExecuteIntent
import uk.co.thomasc.thealley.web.google.ExecuteResponse
import uk.co.thomasc.thealley.web.google.ExecuteResponseCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode
import uk.co.thomasc.thealley.web.google.GoogleHomeReq
import uk.co.thomasc.thealley.web.google.GoogleHomeRes
import uk.co.thomasc.thealley.web.google.QueryIntent
import uk.co.thomasc.thealley.web.google.QueryResponse
import uk.co.thomasc.thealley.web.google.QueryStatus
import uk.co.thomasc.thealley.web.google.SyncIntent
import uk.co.thomasc.thealley.web.google.SyncResponse
import java.util.UUID

class ExternalHandler(private val bus: AlleyEventBus, private val deviceMapper: AlleyDeviceMapper) {
    private val defaultStatus = ExecuteStatus.DEFAULT

    init {
        CoroutineScope(threadPool).launch {
            bus.handle<ReportStateEvent> {
                val device: uk.co.thomasc.thealley.devices.AlleyDevice<*, *, *> =
                    deviceMapper.getDevice(it.deviceId) ?: throw Exception("Device not found")

                if (!FollowUpAuth.canFollowUp()) return@handle

                sendFollowUp(
                    FollowUpResponse(
                        "top_cat",
                        requestId = UUID.randomUUID().toString(),
                        payload = FollowUpPayload(
                            FollowUpDevices(
                                states = mapOf(
                                    device.id.toString() to JsonObject(getState(device, setOf()))
                                )
                            )
                        )
                    )
                )
            }
        }
    }

    private suspend fun sendFollowUp(body: FollowUpResponse) {
        val json = alleyJson.encodeToString(body)
        logger.info { "Sending follow up $json" }

        client.post("https://homegraph.googleapis.com/v1/devices:reportStateAndNotification") {
            bearerAuth(FollowUpAuth.getToken()!!)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    private fun handleFollowUp(userId: String, requestId: String, deviceId: String, trait: String, token: String, result: IFollowUp) {
        if (!FollowUpAuth.canFollowUp()) return

        CoroutineScope(threadPool).launch {
            val followUpJson = alleyJson.encodeToJsonElement(result)
            val followUpObject = followUpJson.jsonObject
                .minus("type")
                .plus("followUpToken" to JsonPrimitive(token))
            val eventId = UUID.randomUUID()

            sendFollowUp(
                FollowUpResponse(
                    userId,
                    eventId.toString(),
                    requestId,
                    FollowUpPayload(
                        FollowUpDevices(
                            mapOf(
                                deviceId to mapOf(
                                    trait to FollowUpNotification(0, JsonObject(followUpObject))
                                )
                            )
                        )
                    )
                )
            )
        }
    }

    // TODO: Split this up to make more readable
    private suspend fun executeRequest(userId: String, requestId: String, intent: ExecuteIntent) = ExecuteResponse(
        intent.payload.commands.map { cmd -> // Execute commands
            coroutineScope {
                cmd.devices.map { device ->
                    async(threadPool) {
                        device to cmd.execution.map { ex ->
                            val dev = deviceMapper.getDevice(device.deviceId)

                            dev?.gh?.let { g ->
                                g.traits.firstNotNullOfOrNull { trait ->
                                    trait.handleUnsafe(ex) {
                                        if (ex is IGoogleHomeFollowUpCommand) {
                                            handleFollowUp(
                                                userId,
                                                requestId,
                                                device.id,
                                                trait.name.substringAfterLast('.'),
                                                ex.params.followUpToken,
                                                it
                                            )
                                        } else {
                                            throw IllegalStateException("Can't follow up this command")
                                        }
                                    }
                                }
                            } ?: ExecuteStatus.ERROR(GoogleHomeErrorCode.DeviceNotFound)
                        }
                    }
                }
            }
        }.flatMap { cmd -> // Collate results
            // Commands -> Devices -> Executions
            cmd.map { localDevices -> // Wait for execution
                localDevices.await()
            }.map { (device, results) -> // Combine execution results
                device to results.fold<ExecuteStatus, ExecuteStatus>(defaultStatus) { acc, v ->
                    acc.combine(v)
                }
            }.map { (device, result) -> // Get state for successful executions
                // val dev = deviceMapper.getDevice(device.deviceId)

                device to if (result is ExecuteStatus.SUCCESS) {
                    /*coroutineScope {
                        dev?.let { result to async(threadPool) { getState(dev, false) } } ?: (ExecuteStatus.ERROR(GoogleHomeErrorCode.DeviceNotFound) to null)
                    }*/
                    result to result.state
                } else {
                    result to null
                }
            }
        }.groupBy({ it.second }, { it.first }).map { (f, devices) ->
            val (status, states) = f
            ExecuteResponseCommand(
                devices.map { device -> device.id },
                status.name,
                if (status is ExecuteStatus.SUCCESS && states != null) JsonObject(states) else null,
                if (status is ExecuteStatus.ERROR) status.errorCode else null
            )
        }
    )

    private suspend fun getState(device: uk.co.thomasc.thealley.devices.AlleyDevice<*, *, *>, defaultKeys: Set<String> = setOf("online", "status", "errorCode")) =
        device.gh?.let { g ->
            g.traits.fold(
                mapOf<String, JsonElement>(
                    "online" to JsonPrimitive(true),
                    "status" to JsonPrimitive(QueryStatus.SUCCESS.name)
                ).filter { defaultKeys.contains(it.key) }
            ) { a, b ->
                a.plus(b.getState())
            }
        } ?: mapOf(
            "online" to JsonPrimitive(false),
            "status" to JsonPrimitive(QueryStatus.ERROR.name),
            "errorCode" to JsonPrimitive("deviceOffline")
        ).filter { defaultKeys.contains(it.key) }

    private suspend fun queryRequest(intent: QueryIntent) = QueryResponse(
        intent.payload.devices.mapNotNull {
            deviceMapper.getDevice(it.deviceId)
        }.associate {
            it.id.toString() to JsonObject(getState(it))
        }
    )

    private suspend fun syncRequest(userId: String) = SyncResponse(
        userId,
        devices = deviceMapper.getDevices().mapNotNull {
            it.gh?.let { g ->
                AlleyDevice(
                    it.id.toString(),
                    g.type,
                    g.traits.map { t -> t.name }.toSet(),
                    AlleyDeviceNames(name = it.config.name),
                    g.willReportState,
                    attributes = g.traits.fold(mapOf()) { a, b ->
                        a.plus(b.getAttributes())
                    }
                )
            }
        }
    )

    suspend fun handleRequest(userId: String, req: GoogleHomeReq) =
        GoogleHomeRes(
            req.requestId,
            when (val intent = req.inputs.first()) {
                is SyncIntent -> syncRequest(userId)
                is QueryIntent -> queryRequest(intent)
                is ExecuteIntent -> executeRequest(userId, req.requestId, intent)
                is DisconnectIntent -> DisconnectResponse()
            }.encodeToJson(alleyJson)
        )

    companion object : KLogging() {
        val threadPool = newFixedThreadPoolContext(10, "ExternalRoute")
    }
}
