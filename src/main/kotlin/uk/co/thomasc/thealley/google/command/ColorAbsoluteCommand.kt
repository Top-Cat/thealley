package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IAlleyLight
import uk.co.thomasc.thealley.google.trait.IColorState
import uk.co.thomasc.thealley.web.google.DeviceColorCommand
import java.awt.Color

@Serializable
@SerialName("action.devices.commands.ColorAbsolute")
data class ColorAbsoluteCommand(override val params: Params) : IGoogleHomeCommand<ColorAbsoluteCommand.Params> {
    @Serializable
    data class Params(
        val color: DeviceColorCommand
    ) {
        val state = when {
            color.temperature != null -> IColorCommandState.Temperature(color.name, color.temperature)
            color.spectrumRgb != null -> IColorCommandState.Rgb(color.name, color.spectrumRgb)
            color.spectrumHsv != null -> IColorCommandState.Hsv(color.name, IColorCommandState.Hsv.Spectrum(color.spectrumHsv.hue, color.spectrumHsv.saturation, color.spectrumHsv.value))
            else -> throw IllegalArgumentException("Invalid color command")
        }
    }
}

sealed interface IColorCommandState {
    val name: String?
    suspend fun setComplexState(bus: AlleyEventBus, block: suspend (AlleyEventBus, IAlleyLight.LightState, Int?) -> Unit)
    fun toState(): IColorState

    @Serializable
    data class Temperature(override val name: String? = null, val temperature: Int) : IColorCommandState {
        override suspend fun setComplexState(bus: AlleyEventBus, block: suspend (AlleyEventBus, IAlleyLight.LightState, Int?) -> Unit) {
            block(bus, IAlleyLight.LightState(temperature = temperature), null)
        }

        override fun toState() = IColorState.Temperature(temperature)
    }

    @Serializable
    data class Rgb(override val name: String? = null, val spectrumRGB: Int) : IColorCommandState {
        val r: Int = (spectrumRGB shr 16) and 255
        val g: Int = (spectrumRGB shr 8) and 255
        val b: Int = spectrumRGB and 255

        override suspend fun setComplexState(bus: AlleyEventBus, block: suspend (AlleyEventBus, IAlleyLight.LightState, Int?) -> Unit) {
            val color = Color.RGBtoHSB(r, g, b, null)

            block(
                bus,
                IAlleyLight.LightState(
                    (color[2] * 100).toInt(),
                    (color[0] * 360).toInt(),
                    (color[1] * 100).toInt()
                ),
                null
            )
        }

        override fun toState() = IColorState.Rgb(spectrumRGB)
    }

    @Serializable
    data class Hsv(override val name: String? = null, val spectrumHsv: Spectrum) : IColorCommandState {
        @Serializable
        data class Spectrum(val hue: Float, val saturation: Float, val value: Float)

        override suspend fun setComplexState(bus: AlleyEventBus, block: suspend (AlleyEventBus, IAlleyLight.LightState, Int?) -> Unit) {
            block(
                bus,
                IAlleyLight.LightState(
                    (spectrumHsv.value * 100).toInt(),
                    spectrumHsv.hue.toInt(),
                    (spectrumHsv.saturation * 100).toInt()
                ),
                null
            )
        }

        override fun toState() = IColorState.Hsv(IColorState.Hsv.Spectrum(spectrumHsv.hue, spectrumHsv.saturation, spectrumHsv.value))
    }
}
