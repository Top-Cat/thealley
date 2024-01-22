package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.google.trait.GoogleHomeTrait
import kotlin.reflect.KClass

class MissingTraitException(kClass: KClass<out GoogleHomeTrait<out IGoogleHomeCommand<*>>>) : Exception("Missing required trait: ${kClass.simpleName}")
