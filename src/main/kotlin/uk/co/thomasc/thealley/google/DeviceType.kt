package uk.co.thomasc.thealley.google

import kotlinx.serialization.SerialName
import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.google.trait.GoogleHomeTrait
import uk.co.thomasc.thealley.google.trait.InputSelectorTrait
import uk.co.thomasc.thealley.google.trait.MediaStateTrait
import uk.co.thomasc.thealley.google.trait.NetworkControlTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait
import uk.co.thomasc.thealley.google.trait.SceneTrait
import uk.co.thomasc.thealley.google.trait.TransportControlTrait
import uk.co.thomasc.thealley.google.trait.VolumeTrait
import kotlin.reflect.KClass

enum class DeviceType(val requiredTraits: Set<KClass<out GoogleHomeTrait<out IGoogleHomeCommand<*>>>>) {
    @SerialName("action.devices.types.AUDIO_VIDEO_RECEIVER")
    AUDIO_VIDEO_RECEIVER(setOf(InputSelectorTrait::class, MediaStateTrait::class, OnOffTrait::class, TransportControlTrait::class, VolumeTrait::class)),

    @SerialName("action.devices.types.BLINDS")
    BLINDS(setOf(OpenCloseTrait::class)),

    @SerialName("action.devices.types.LIGHT")
    LIGHT(setOf(OnOffTrait::class)),

    @SerialName("action.devices.types.NETWORK")
    NETWORK(setOf(NetworkControlTrait::class)),

    @SerialName("action.devices.types.SCENE")
    SCENE(setOf(SceneTrait::class))
}
