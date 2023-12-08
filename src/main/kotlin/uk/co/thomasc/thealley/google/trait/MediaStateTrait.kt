package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.google.command.NoCommands
import uk.co.thomasc.thealley.web.google.ExecuteStatus

class MediaStateTrait(
    private val supportActivityState: Boolean = false,
    private val supportPlaybackState: Boolean = false,
    private val getActivityState: (suspend () -> ActivityState)? = null,
    private val getPlaybackState: (suspend () -> PlaybackState)? = null
) : GoogleHomeTrait<NoCommands>() {
    override val name = "action.devices.traits.MediaState"
    override val klazz = NoCommands::class

    override suspend fun getAttributes() = mapOf(
        "supportActivityState" to JsonPrimitive(supportActivityState),
        "supportPlaybackState" to JsonPrimitive(supportPlaybackState)
    )

    override suspend fun getState() = mapOf<String, JsonElement>().let {
        getActivityState?.invoke()?.name?.let { state ->
            it.plus("activityState" to JsonPrimitive(state))
        } ?: it
    }.let {
        getPlaybackState?.invoke()?.name?.let { state ->
            it.plus("playbackState" to JsonPrimitive(state))
        } ?: it
    }

    override suspend fun handleCommand(cmd: NoCommands) = ExecuteStatus.SUCCESS()

    enum class ActivityState {
        INACTIVE, STANDBY, ACTIVE
    }

    enum class PlaybackState {
        PAUSED, PLAYING, FAST_FORWARDING, REWINDING, BUFFERING, STOPPED
    }
}
