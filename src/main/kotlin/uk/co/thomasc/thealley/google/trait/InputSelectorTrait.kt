package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.command.IInputSelectorCommand
import uk.co.thomasc.thealley.google.command.NextInputCommand
import uk.co.thomasc.thealley.google.command.PreviousInputCommand
import uk.co.thomasc.thealley.google.command.SetInputCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus

class InputSelectorTrait(
    private val commandOnlyInputSelector: Boolean = false,
    private val orderedInputs: Boolean = false,
    private val getInputs: suspend () -> List<InputSelectorInput>,
    private val getCurrentInput: suspend () -> String,
    private val setInput: suspend (String) -> Unit,
    private val nextInput: (suspend () -> Unit)? = null,
    private val previousInput: (suspend () -> Unit)? = null
) : GoogleHomeTrait<IInputSelectorCommand<*>>() {
    override val name = "action.devices.traits.InputSelector"
    override val klazz = IInputSelectorCommand::class

    override suspend fun getAttributes() = mapOf(
        "commandOnlyInputSelector" to JsonPrimitive(commandOnlyInputSelector),
        "orderedInputs" to JsonPrimitive(orderedInputs),
        "availableInputs" to alleyJson.encodeToJsonElement(getInputs())
    )

    override suspend fun getState() = mapOf(
        "currentInput" to JsonPrimitive(getCurrentInput())
    )

    override suspend fun handleCommand(cmd: IInputSelectorCommand<*>): ExecuteStatus {
        when (cmd) {
            is SetInputCommand -> setInput(cmd.params.newInput)
            is NextInputCommand -> nextInput?.invoke()
            is PreviousInputCommand -> previousInput?.invoke()
        }

        return ExecuteStatus.SUCCESS
    }
}
