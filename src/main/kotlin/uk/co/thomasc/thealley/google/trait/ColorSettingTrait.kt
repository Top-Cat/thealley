package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.devicev2.IAlleyLight
import uk.co.thomasc.thealley.google.command.ColorAbsoluteCommand
import uk.co.thomasc.thealley.google.command.IColorCommandState
import uk.co.thomasc.thealley.rest.ExecuteStatus

class ColorSettingTrait(
    private val commandOnlyColorSetting: Boolean = false,
    private val colorModel: ColorModel = ColorModel.HSV,
    private val temperatureMinK: Int? = 2500,
    private val temperatureMaxK: Int? = 9000,
    private val getColor: suspend () -> IColorState,
    private val setColor: suspend (IColorCommandState) -> Unit
) : IGoogleHomeTrait<ColorAbsoluteCommand> {
    enum class ColorModel(val json: String) {
        RGB("rgb"), HSV("hsv")
    }

    override val name = "action.devices.traits.ColorSetting"

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

sealed interface IColorState {
    @Serializable
    data class Temperature(val temperatureK: Int) : IColorState

    @Serializable
    data class Rgb(val spectrumRgb: Int) : IColorState {
        constructor(r: Int, g: Int, b: Int) : this(((r and 255) shl 16) or ((g and 255) shl 8) or (b and 255))
        constructor(r: Byte, g: Byte, b: Byte) : this(r.toInt(), g.toInt(), b.toInt())
    }

    @Serializable
    data class Hsv(val spectrumHsv: Spectrum) : IColorState {
        @Serializable
        data class Spectrum(val hue: Float, val saturation: Float, val value: Float)
        constructor(hue: Int, saturation: Float, value: Float) : this(hue.toFloat(), saturation, value)
        constructor(hue: Float, saturation: Float, value: Float) : this(Spectrum(hue, saturation, value))
    }

    companion object {
        fun fromLightState(lightState: IAlleyLight.LightState) =
            if (lightState.temperature?.let { it > 0 } == true) {
                Temperature(lightState.temperature)
            } else {
                Hsv(
                    lightState.hue?.toFloat() ?: 0f,
                    (lightState.saturation ?: 0) / 100f,
                    (lightState.brightness ?: 0) / 100f
                )
            }
    }
}
