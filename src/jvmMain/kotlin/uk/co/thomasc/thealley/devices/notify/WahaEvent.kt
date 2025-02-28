package uk.co.thomasc.thealley.devices.notify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class WahaEvent<T : WahaEventPayload>(
    val id: String,
    val event: WahaEventType,
    val session: String,
    val me: WahaMe,
    val payload: T,
    val engine: WahaEngine,
    val environment: WahaEnvironment
)

@Serializable
data class WahaMe(
    val id: String,
    val pushName: String
)

@Serializable
data class WahaEnvironment(
    val version: String,
    val engine: WahaEngine,
    val tier: String,
    val browser: String
)

enum class WahaEventType {
    @SerialName("message")
    MESSAGE
}

enum class WahaEngine {
    NOWEB, CHROME
}

interface WahaEventPayload

@Serializable
data class WahaMessage(
    val id: String,
    val timestamp: Long,
    val from: String,
    val fromMe: Boolean,
    val body: String,
    val hasMedia: Boolean,
    val media: WahaMedia?,
    val ack: Int?,
    val ackName: String,
    val replyTo: String?,
    @SerialName("_data")
    val data: WahaMessageData
) : WahaEventPayload

@Serializable
data class WahaMedia(
    val url: String,
    val mimetype: String,
    val filename: String?,
    val error: JsonElement?
)

@Serializable
data class WahaMessageData(
    val key: WahaMessageDataKey,
    val messageTimestamp: Long,
    val pushName: String,
    val broadcast: Boolean,
    val message: WahaMessageDataMessage
)

@Serializable
data class WahaMessageDataKey(
    val remoteJid: String,
    val fromMe: Boolean,
    val id: String
)

@Serializable
data class WahaMessageDataMessage(
    val conversation: String,
    val messageContextInfo: WahaMessageDataMessageContextInfo
)

@Serializable
data class WahaMessageDataMessageContextInfo(
    val deviceListMetadata: WahaDeviceListMetadata,
    val deviceListMetadataVersion: Int,
    val messageSecret: String
)

@Serializable
data class WahaDeviceListMetadata(
    val senderKeyHash: String,
    val senderTimestamp: Long,
    val senderAccountType: String?,
    val receiverAccountType: String?,
    val recipientKeyHash: String,
    val recipientTimestamp: Long
)
