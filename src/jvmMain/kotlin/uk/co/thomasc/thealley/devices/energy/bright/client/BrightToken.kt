package uk.co.thomasc.thealley.devices.energy.bright.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BrightToken(
    val valid: Boolean,
    val token: String,
    val exp: Long,
    val userGroups: List<JsonElement>,
    val functionalGroupAccounts: List<JsonElement>,
    val accountId: String,
    val isTempAuth: Boolean,
    val name: String
)
