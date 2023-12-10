package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonElement
import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.google.command.IGoogleHomeCommandBase
import uk.co.thomasc.thealley.google.followup.IFollowUpHandler
import uk.co.thomasc.thealley.web.google.ExecuteStatus
import kotlin.reflect.KClass

sealed class GoogleHomeTrait<T : IGoogleHomeCommand<*>> {
    abstract val name: String
    protected abstract val klazz: KClass<T>
    abstract suspend fun getAttributes(): Map<String, JsonElement>
    abstract suspend fun getState(): Map<String, JsonElement>
    open suspend fun handleCommand(cmd: T): ExecuteStatus =
        ExecuteStatus.SUCCESS()
    open suspend fun handleCommand(cmd: T, followUp: IFollowUpHandler) =
        handleCommand(cmd)

    @Suppress("UNCHECKED_CAST")
    internal suspend fun handleUnsafe(cmd: IGoogleHomeCommandBase, followUp: IFollowUpHandler) =
        if (klazz.isInstance(cmd)) {
            handleCommand(cmd as T, followUp)
        } else {
            null
        }
}
