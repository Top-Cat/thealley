package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.command.BrightnessAbsoluteCommand
import uk.co.thomasc.thealley.google.command.BrightnessRelativeCommand
import uk.co.thomasc.thealley.google.command.IBrightnessCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode

class BrightnessTrait(
    private val commandOnlyBrightness: Boolean = false,
    private val getBrightness: suspend () -> Int,
    private val setBrightness: suspend (Int) -> Unit,
    private val setBrightnessPercentage: (suspend (Int) -> Int)? = null,
    private val setBrightnessWeight: (suspend (Int) -> Int)? = null
) : GoogleHomeTrait<IBrightnessCommand<*>>() {
    override val name = "action.devices.traits.Brightness"
    override val klazz = IBrightnessCommand::class

    override suspend fun getAttributes() = mapOf(
        "commandOnlyBrightness" to JsonPrimitive(commandOnlyBrightness)
    )

    @Serializable
    data class BrightnessState(val brightness: Int)

    private fun mapFor(state: BrightnessState) = alleyJson.encodeToJsonElement(state).jsonObject

    override suspend fun getState() = mapFor(BrightnessState(getBrightness()))

    override suspend fun handleCommand(cmd: IBrightnessCommand<*>) =
        when (cmd) {
            is BrightnessAbsoluteCommand -> {
                setBrightness(cmd.params.brightness)
                ExecuteStatus.SUCCESS(
                    mapFor(BrightnessState(cmd.params.brightness))
                )
            }
            is BrightnessRelativeCommand -> {
                if (cmd.params.brightnessRelativePercent != null) {
                    setBrightnessPercentage?.let {
                        val newBrightness = it(cmd.params.brightnessRelativePercent)
                        ExecuteStatus.SUCCESS(
                            mapFor(BrightnessState(newBrightness))
                        )
                    } ?: ExecuteStatus.ERROR(GoogleHomeErrorCode.FunctionNotSupported)
                } else if (cmd.params.brightnessRelativeWeight != null) {
                    setBrightnessWeight?.let {
                        val newBrightness = it(cmd.params.brightnessRelativeWeight)
                        ExecuteStatus.SUCCESS(
                            mapFor(BrightnessState(newBrightness))
                        )
                    } ?: ExecuteStatus.ERROR(GoogleHomeErrorCode.FunctionNotSupported)
                } else {
                    throw IllegalArgumentException("Either percent of weight must be set")
                }
            }
        }
}
