package uk.co.thomasc.thealley.google

import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.google.trait.IGoogleHomeTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait
import uk.co.thomasc.thealley.google.trait.SceneTrait
import kotlin.reflect.KClass

enum class DeviceType(val typeName: String, val requiredTraits: Set<KClass<out IGoogleHomeTrait<out IGoogleHomeCommand<*>>>>) {
    BLIND("action.devices.types.BLIND", setOf(OpenCloseTrait::class)),
    LIGHT("action.devices.types.LIGHT", setOf(OnOffTrait::class)),
    SCENE("action.devices.types.SCENE", setOf(SceneTrait::class))
}
