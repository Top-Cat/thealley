package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.google.command.IOpenCloseCommand
import uk.co.thomasc.thealley.google.command.OpenCloseCommand
import uk.co.thomasc.thealley.google.command.OpenCloseRelativeCommand
import uk.co.thomasc.thealley.rest.ExecuteStatus

class OpenCloseTrait(
    private val discreteOnlyOpenClose: Boolean = false,
    private val openDirection: Set<Direction> = setOf(),
    private val commandOnlyOpenClose: Boolean = false,
    private val queryOnlyOpenClose: Boolean = false,
    private val getPosition: suspend () -> IBlindState,
    private val setPosition: suspend (Int) -> Unit,
    private val setPositionRelative: (suspend (Int) -> Unit)? = null
) : IGoogleHomeTrait<IOpenCloseCommand<*>> {
    enum class Direction {
        UP, DOWN, LEFT, RIGHT, IN, OUT
    }

    override val name = "action.devices.traits.OpenClose"

    override suspend fun getAttributes() = mapOf(
        "discreteOnlyOpenClose" to JsonPrimitive(discreteOnlyOpenClose),
        "commandOnlyOpenClose" to JsonPrimitive(commandOnlyOpenClose),
        "queryOnlyOpenClose" to JsonPrimitive(queryOnlyOpenClose)
    ).let {
        if (openDirection.size > 1) {
            it.plus(
                "openDirection" to JsonArray(openDirection.map { d -> JsonPrimitive(d.name) })
            )
        } else {
            it
        }
    }

    override suspend fun getState() = when (val s = getPosition()) {
        is IBlindState.MultipleDirections -> alleyJson.encodeToJsonElement(s)
        is IBlindState.SingleDirection -> alleyJson.encodeToJsonElement(s)
    }.jsonObject

    override suspend fun handleCommand(cmd: IOpenCloseCommand<*>): ExecuteStatus {
        when (cmd) {
            is OpenCloseCommand -> setPosition(cmd.params.openPercent)
            is OpenCloseRelativeCommand -> setPositionRelative?.invoke(cmd.params.openRelativePercent)
        }

        return ExecuteStatus.SUCCESS
    }
}

sealed interface IBlindState {
    @Serializable
    data class SingleDirection(val openPercent: Int) : IBlindState

    @Serializable
    data class MultipleDirections(val openState: List<DirectionInfo>) : IBlindState

    @Serializable
    data class DirectionInfo(val openPercent: Int, val openDirection: OpenCloseTrait.Direction)
}
