package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.google.command.IVolumeCommand
import uk.co.thomasc.thealley.google.command.MuteCommand
import uk.co.thomasc.thealley.google.command.SetVolumeCommand
import uk.co.thomasc.thealley.google.command.VolumeRelativeCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode

class VolumeTrait(
    private val volumeMaxLevel: Int,
    private val volumeCanMuteAndUnmute: Boolean,
    private val volumeDefaultPercentage: Int = 40,
    private val levelStepSize: Int = 1,
    private val commandOnlyVolume: Boolean = false,
    private val getVolume: suspend () -> Int,
    private val isMuted: suspend () -> Boolean,
    private val mute: suspend (Boolean) -> Boolean?,
    private val setVolume: suspend (Int) -> Int?,
    private val setVolumeRelative: (suspend (Int) -> Int?)? = null
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

    override suspend fun handleCommand(cmd: IVolumeCommand<*>) =
        when (cmd) {
            is MuteCommand -> {
                val isMuted = mute(cmd.params.mute)

                if (isMuted == null) {
                    ExecuteStatus.ERROR(GoogleHomeErrorCode.DeviceTurnedOff)
                } else {
                    ExecuteStatus.SUCCESS(
                        mapOf(
                            "isMuted" to JsonPrimitive(isMuted)
                        )
                    )
                }
            }
            is SetVolumeCommand -> {
                val newVolume = setVolume(cmd.params.volumeLevel)

                if (newVolume == null) {
                    ExecuteStatus.ERROR(GoogleHomeErrorCode.DeviceTurnedOff)
                } else {
                    ExecuteStatus.SUCCESS(
                        mapOf(
                            "currentVolume" to JsonPrimitive(newVolume)
                        )
                    )
                }
            }
            is VolumeRelativeCommand -> {
                setVolumeRelative?.let {
                    val newVolume = it(cmd.params.relativeSteps)

                    if (newVolume == null) {
                        ExecuteStatus.ERROR(GoogleHomeErrorCode.DeviceTurnedOff)
                    } else {
                        ExecuteStatus.SUCCESS(
                            mapOf(
                                "currentVolume" to JsonPrimitive(newVolume)
                            )
                        )
                    }
                } ?: ExecuteStatus.ERROR(GoogleHomeErrorCode.FunctionNotSupported)
            }
        }
}
