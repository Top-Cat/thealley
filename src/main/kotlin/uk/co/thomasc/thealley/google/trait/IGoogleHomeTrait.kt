package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonElement
import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.rest.ExecuteStatus

sealed interface IGoogleHomeTrait<T : IGoogleHomeCommand<*>> {
    val name: String
    suspend fun getAttributes(): Map<String, JsonElement>
    suspend fun getState(): Map<String, JsonElement>
    suspend fun handleCommand(cmd: T): ExecuteStatus
}
