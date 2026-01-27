package uk.co.thomasc.thealley.devices.notify

import kotlinx.serialization.Serializable

@Serializable
data class NtfyPub(
    val topic: String,
    val message: String? = null,
    val title: String? = null,
    val tags: List<String>? = null,
    val priority: Int? = null,
    val actions: List<NtfyAction>? = null,
    val click: String? = null,
    val attach: String? = null,
    val markdown: Boolean? = null,
    val icon: String? = null,
    val filename: String? = null,
    val delay: String? = null,
    val email: String? = null,
    val call: String? = null
)

interface NtfyAction {
    val action: String
    val label: String
    val clear: Boolean?
}

@Serializable
data class NtfyViewAction(
    override val label: String,
    val url: String,
    override val clear: Boolean? = null
) : NtfyAction {
    override val action = "view"
}

@Serializable
data class NtfyBroadcastAction(
    override val label: String,
    val intent: String? = null,
    val extras: Map<String, String>? = null,
    override val clear: Boolean? = null
) : NtfyAction {
    override val action = "broadcast"
}

@Serializable
data class NtfyHttpAction(
    override val label: String,
    val url: String,
    val method: String? = null,
    val headers: Map<String, String>? = null,
    val body: String? = null,
    override val clear: Boolean? = null
) : NtfyAction {
    override val action = "http"
}
