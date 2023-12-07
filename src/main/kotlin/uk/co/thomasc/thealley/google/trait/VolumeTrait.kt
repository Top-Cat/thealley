package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.google.command.IVolumeCommand
import uk.co.thomasc.thealley.google.command.MuteCommand
import uk.co.thomasc.thealley.google.command.SetVolumeCommand
import uk.co.thomasc.thealley.google.command.VolumeRelativeCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus

class VolumeTrait(
    private val volumeMaxLevel: Int,
    private val volumeCanMuteAndUnmute: Boolean,
    private val volumeDefaultPercentage: Int = 40,
    private val levelStepSize: Int = 1,
    private val commandOnlyVolume: Boolean = false,
    private val getVolume: suspend () -> Int,
    private val isMuted: suspend () -> Boolean,
    private val mute: suspend (Boolean) -> Unit,
    private val setVolume: suspend (Int) -> Unit,
    private val setVolumeRelative: (suspend (Int) -> Unit)? = null
) : GoogleHomeTrait<IVolumeCommand<*>>() {
    override val name = "action.devices.traits.Volume"
    override val klazz = IVolumeCommand::class

    override suspend fun getAttributes() = mapOf(
        "volumeMaxLevel" to JsonPrimitive(volumeMaxLevel),
        "volumeCanMuteAndUnmute" to JsonPrimitive(volumeCanMuteAndUnmute),
        "volumeDefaultPercentage" to JsonPrimitive(volumeDefaultPercentage),
        "levelStepSize" to JsonPrimitive(levelStepSize),
        "commandOnlyVolume" to JsonPrimitive(commandOnlyVolume)
    )

    override suspend fun getState() = mapOf(
        "currentVolume" to JsonPrimitive(getVolume()),
        "isMuted" to JsonPrimitive(isMuted())
    )

    override suspend fun handleCommand(cmd: IVolumeCommand<*>): ExecuteStatus {
        when (cmd) {
            is MuteCommand -> mute(cmd.params.mute)
            is SetVolumeCommand -> setVolume(cmd.params.volumeLevel)
            is VolumeRelativeCommand -> setVolumeRelative?.invoke(cmd.params.relativeSteps)
        }

        return ExecuteStatus.SUCCESS
    }
}
