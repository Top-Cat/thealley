package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.IAlleyLight

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
