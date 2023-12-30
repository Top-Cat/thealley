package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Scene")
data class SceneConfig(override val name: String, val parts: List<ScenePart>) : IAlleyConfig {
    @Serializable
    class ScenePart(val lightId: Int, val brightness: Int, val hue: Int? = null, val saturation: Int? = null, val temperature: Int? = null)
}
