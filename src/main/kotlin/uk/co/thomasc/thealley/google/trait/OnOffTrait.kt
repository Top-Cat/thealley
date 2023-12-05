package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.google.command.OnOffCommand
import uk.co.thomasc.thealley.rest.ExecuteStatus

class OnOffTrait(
    private val commandOnlyOnOff: Boolean = false,
    private val queryOnlyOnOff: Boolean = false,
    private val getOnOff: suspend () -> Boolean,
    private val setOnOff: suspend (Boolean) -> Unit
) : GoogleHomeTrait<OnOffCommand>() {
    override val name = "action.devices.traits.OnOff"
    override val klazz = OnOffCommand::class

    override suspend fun getAttributes() = mapOf(
        "commandOnlyOnOff" to JsonPrimitive(commandOnlyOnOff),
        "queryOnlyOnOff" to JsonPrimitive(queryOnlyOnOff)
    )

    override suspend fun getState() = mapOf(
        "on" to JsonPrimitive(getOnOff())
    )

    override suspend fun handleCommand(cmd: OnOffCommand): ExecuteStatus {
        setOnOff(cmd.params.on)

        return ExecuteStatus.SUCCESS
    }
}
