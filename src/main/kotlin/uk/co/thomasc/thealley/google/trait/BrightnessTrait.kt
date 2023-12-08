package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.google.command.BrightnessAbsoluteCommand
import uk.co.thomasc.thealley.google.command.BrightnessRelativeCommand
import uk.co.thomasc.thealley.google.command.IBrightnessCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus

class BrightnessTrait(
    private val commandOnlyBrightness: Boolean = false,
    private val getBrightness: suspend () -> Int,
    private val setBrightness: suspend (Int) -> Unit,
    private val setBrightnessPercentage: (suspend (Int) -> Unit)? = null,
    private val setBrightnessWeight: (suspend (Int) -> Unit)? = null
) : GoogleHomeTrait<IBrightnessCommand<*>>() {
    override val name = "action.devices.traits.Brightness"
    override val klazz = IBrightnessCommand::class

    override suspend fun getAttributes() = mapOf(
        "commandOnlyBrightness" to JsonPrimitive(commandOnlyBrightness)
    )

    override suspend fun getState() = mapOf(
        "brightness" to JsonPrimitive(getBrightness())
    )

    override suspend fun handleCommand(cmd: IBrightnessCommand<*>): ExecuteStatus {
        when (cmd) {
            is BrightnessAbsoluteCommand -> setBrightness(cmd.params.brightness)
            is BrightnessRelativeCommand -> {
                if (cmd.params.brightnessRelativePercent != null) {
                    setBrightnessPercentage?.invoke(cmd.params.brightnessRelativePercent)
                } else if (cmd.params.brightnessRelativeWeight != null) {
                    setBrightnessWeight?.invoke(cmd.params.brightnessRelativeWeight)
                } else {
                    throw IllegalArgumentException("Either percent of weight must be set")
                }
            }
        }

        return ExecuteStatus.SUCCESS()
    }
}
