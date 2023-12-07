package uk.co.thomasc.thealley.google

import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.google.trait.GoogleHomeTrait
import uk.co.thomasc.thealley.google.trait.InputSelectorTrait
import uk.co.thomasc.thealley.google.trait.MediaStateTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait
import uk.co.thomasc.thealley.google.trait.SceneTrait
import uk.co.thomasc.thealley.google.trait.TransportControlTrait
import uk.co.thomasc.thealley.google.trait.VolumeTrait
import kotlin.reflect.KClass

enum class DeviceType(val typeName: String, val requiredTraits: Set<KClass<out GoogleHomeTrait<out IGoogleHomeCommand<*>>>>) {
    AUDIO_VIDEO_RECEIVER(
        "action.devices.types.AUDIO_VIDEO_RECEIVER",
        setOf(InputSelectorTrait::class, MediaStateTrait::class, OnOffTrait::class, TransportControlTrait::class, VolumeTrait::class)
    ),
    BLINDS("action.devices.types.BLINDS", setOf(OpenCloseTrait::class)),
    LIGHT("action.devices.types.LIGHT", setOf(OnOffTrait::class)),
    SCENE("action.devices.types.SCENE", setOf(SceneTrait::class))
}
