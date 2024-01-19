package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.command.NoCommands
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode

class StatusReportTrait(
    private val getReport: () -> Set<State>
) : GoogleHomeTrait<NoCommands>() {
    override val name = "action.devices.traits.StatusReport"
    override val klazz = NoCommands::class

    @Serializable
    data class State(
        val blocking: Boolean,
        val deviceTarget: String,
        val priority: Int,
        val statusCode: GoogleHomeErrorCode? = null
    )

    override suspend fun getAttributes() = emptyMap<String, JsonElement>()

    override suspend fun getState() = alleyJson.encodeToJsonElement(getReport()).jsonObject

    companion object : KLogging()
}
