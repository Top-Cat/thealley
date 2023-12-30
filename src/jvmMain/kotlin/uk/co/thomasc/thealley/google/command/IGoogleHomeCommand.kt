package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("command")
sealed interface IGoogleHomeCommandBase {
    val params: Any
}

interface IGoogleHomeFollowUpCommand {
    val params: IGoogleHomeFollowUpParams
}

interface IGoogleHomeFollowUpParams {
    val followUpToken: String
}

sealed interface IGoogleHomeCommand<out T : Any> : IGoogleHomeCommandBase {
    override val params: T
}

@Serializable
data object NoCommands : IGoogleHomeCommand<NoParams> {
    override val params = NoParams
}

@Serializable
object NoParams
