package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.GoogleHomeLang
import uk.co.thomasc.thealley.google.command.ITransportControlCommand
import uk.co.thomasc.thealley.google.command.MediaClosedCaptioningOffCommand
import uk.co.thomasc.thealley.google.command.MediaClosedCaptioningOnCommand
import uk.co.thomasc.thealley.google.command.MediaNextCommand
import uk.co.thomasc.thealley.google.command.MediaPauseCommand
import uk.co.thomasc.thealley.google.command.MediaPreviousCommand
import uk.co.thomasc.thealley.google.command.MediaRepeatModeCommand
import uk.co.thomasc.thealley.google.command.MediaResumeCommand
import uk.co.thomasc.thealley.google.command.MediaSeekRelativeCommand
import uk.co.thomasc.thealley.google.command.MediaSeekToPositionCommand
import uk.co.thomasc.thealley.google.command.MediaShuffleCommand
import uk.co.thomasc.thealley.google.command.MediaStopCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus

class TransportControlTrait(
    private val transportControlSupportedCommands: List<ControlCommand> = listOf(),
    private val stop: (suspend () -> Unit)? = null,
    private val next: (suspend () -> Unit)? = null,
    private val previous: (suspend () -> Unit)? = null,
    private val pause: (suspend () -> Unit)? = null,
    private val resume: (suspend () -> Unit)? = null,
    private val shuffle: (suspend () -> Unit)? = null,
    private val ccOff: (suspend () -> Unit)? = null,
    private val ccOn: (suspend (GoogleHomeLang?) -> Unit)? = null,
    private val repeat: (suspend (Boolean, Boolean) -> Unit)? = null,
    private val seek: (suspend (Int) -> Unit)? = null,
    private val seekRelative: (suspend (Int) -> Unit)? = null
) : GoogleHomeTrait<ITransportControlCommand<*>>() {
    override val name = "action.devices.traits.TransportControl"
    override val klazz = ITransportControlCommand::class

    override suspend fun getAttributes() = mapOf(
        "transportControlSupportedCommands" to alleyJson.encodeToJsonElement(transportControlSupportedCommands)
    )

    override suspend fun getState() = mapOf<String, JsonElement>()

    override suspend fun handleCommand(cmd: ITransportControlCommand<*>): ExecuteStatus {
        when (cmd) {
            is MediaStopCommand -> stop?.invoke()
            is MediaClosedCaptioningOffCommand -> ccOff?.invoke()
            is MediaClosedCaptioningOnCommand -> ccOn?.invoke(cmd.params.closedCaptioningLanguage)
            is MediaNextCommand -> next?.invoke()
            is MediaPauseCommand -> pause?.invoke()
            is MediaPreviousCommand -> previous?.invoke()
            is MediaRepeatModeCommand -> repeat?.invoke(cmd.params.isOn, cmd.params.isSingle)
            is MediaResumeCommand -> resume?.invoke()
            is MediaSeekRelativeCommand -> seekRelative?.invoke(cmd.params.relativePositionMs)
            is MediaSeekToPositionCommand -> seek?.invoke(cmd.params.absPositionMs)
            is MediaShuffleCommand -> shuffle?.invoke()
        }

        return ExecuteStatus.SUCCESS()
    }

    enum class ControlCommand {
        CAPTION_CONTROL, NEXT, PAUSE, PREVIOUS, RESUME, SEEK_RELATIVE, SEEK_TO_POSITION, SET_REPEAT, SHUFFLE, STOP
    }
}
