package uk.co.thomasc.thealley.web

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
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

class ExternalHandler(private val deviceMapper: AlleyDeviceMapper) {
    private val defaultStatus = ExecuteStatus.DEFAULT

    // TODO: Split this up to make more readable
    private suspend fun executeRequest(intent: ExecuteIntent) = ExecuteResponse(
        intent.payload.commands.map { cmd -> // Execute commands
            coroutineScope {
                cmd.devices.map { device ->
                    async(threadPool) {
                        device to cmd.execution.map { ex ->
                            val dev = deviceMapper.getDevice(device.deviceId)
                            val type = dev?.ghType
                            val traits = dev?.ghTraits

                            if (type != null && traits != null) {
                                traits.firstNotNullOfOrNull { trait ->
                                    trait.handleUnsafe(ex)
                                }
                            } else {
                                null
                            } ?: ExecuteStatus.ERROR(GoogleHomeErrorCode.DeviceNotFound)
                        }
                    }
                }
            }
        }.flatMap { cmd -> // Collate results
            // Commands -> Devices -> Executions
            cmd.map { localDevices ->
                val devices = localDevices.await()
                val dev = deviceMapper.getDevice(devices.first.deviceId)

                val status = devices.second.fold<ExecuteStatus, ExecuteStatus>(defaultStatus) { acc, v ->
                    acc.combine(v)
                }.let {
                    if (it == ExecuteStatus.STATE) {
                        dev?.let { ExecuteStatus.SUCCESS(getState(dev, false)) } ?: ExecuteStatus.ERROR(GoogleHomeErrorCode.DeviceNotFound)
                    } else {
                        it
                    }
                }

                devices.first to status
            }
        }.groupBy({ it.second }, { it.first }).map { (status, devices) ->
            ExecuteResponseCommand(
                devices.map { device -> device.id },
                status.name,
                if (status is ExecuteStatus.SUCCESS) JsonObject(status.state) else null,
                if (status is ExecuteStatus.ERROR) status.errorCode else null
            )
        }
    )

    private suspend fun getState(device: uk.co.thomasc.thealley.devices.AlleyDevice<*, *, *>, queryStatus: Boolean = true): Map<String, JsonElement> {
        val type = device.ghType
        val traits = device.ghTraits

        return if (type != null && traits != null) {
            traits.fold(
                mapOf<String, JsonElement>(
                    "online" to JsonPrimitive(true),
                    "status" to JsonPrimitive(QueryStatus.SUCCESS.name)
                ).filter { queryStatus || it.key == "online" }
            ) { a, b ->
                a.plus(b.getState())
            }
        } else {
            mapOf(
                "online" to JsonPrimitive(false),
                "status" to JsonPrimitive(QueryStatus.ERROR.name),
                "errorCode" to JsonPrimitive("deviceOffline")
            ).filter { queryStatus || it.key == "online" }
        }
    }

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
                is SyncIntent -> syncRequest(userId)
                is QueryIntent -> queryRequest(intent)
                is ExecuteIntent -> executeRequest(intent)
                is DisconnectIntent -> DisconnectResponse()
            }.encodeToJson(alleyJson)
        )

    companion object {
        val threadPool = newFixedThreadPoolContext(10, "ExternalRoute")
    }
}
