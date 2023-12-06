package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.command.ColorAbsoluteCommand
import uk.co.thomasc.thealley.google.command.IColorCommandState
import uk.co.thomasc.thealley.web.google.ExecuteStatus

class ColorSettingTrait(
    private val commandOnlyColorSetting: Boolean = false,
    private val colorModel: ColorModel = ColorModel.HSV,
    private val temperatureMinK: Int? = 2500,
    private val temperatureMaxK: Int? = 9000,
    private val getColor: suspend () -> IColorState,
    private val setColor: suspend (IColorCommandState) -> Unit
) : GoogleHomeTrait<ColorAbsoluteCommand>() {
    enum class ColorModel(val json: String) {
        RGB("rgb"), HSV("hsv")
    }

    override val name = "action.devices.traits.ColorSetting"
    override val klazz = ColorAbsoluteCommand::class

    override suspend fun getAttributes() = mapOf(
        "commandOnlyBrightness" to JsonPrimitive(commandOnlyColorSetting),
        "colorModel" to JsonPrimitive(colorModel.json)
    ).let {
        if (temperatureMinK != null || temperatureMaxK != null) {
            it.plus(
                "colorTemperatureRange" to JsonObject(
                    mapOf(
                        "temperatureMinK" to temperatureMinK,
                        "temperatureMaxK" to temperatureMaxK
                    ).filter { t -> t.value != null }.mapValues { t -> JsonPrimitive(t.value) }
                )
            )
        } else {
            it
        }
    }

    override suspend fun getState() = mapOf(
        "color" to when (val color = getColor()) {
            is IColorState.Rgb -> alleyJson.encodeToJsonElement(color)
            is IColorState.Temperature -> alleyJson.encodeToJsonElement(color)
            is IColorState.Hsv -> alleyJson.encodeToJsonElement(color)
        }
    )

    override suspend fun handleCommand(cmd: ColorAbsoluteCommand): ExecuteStatus {
        setColor(cmd.params.state)

        return ExecuteStatus.SUCCESS
    }
}
