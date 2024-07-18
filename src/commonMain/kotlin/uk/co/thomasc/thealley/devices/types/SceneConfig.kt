package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.IConfigField
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import kotlin.reflect.KProperty1

data class SceneElementConfigField(
    override val name: String,
    val field: KProperty1<SceneConfig, List<SceneConfig.ScenePart>>,
    override val setter: (SceneConfig, List<SceneConfig.ScenePart>) -> SceneConfig
) : IConfigField<SceneConfig, List<SceneConfig.ScenePart>>() {
    override val clazz = SceneConfig::class
    override val getter = { c: SceneConfig -> field.get(c) }
}

@Serializable
@SerialName("Scene")
data class SceneConfig(
    override val name: String,
    val parts: List<ScenePart>
) : IAlleyConfig,
    IAlleyRelayConfig,
    IConfigEditable<SceneConfig> by SimpleConfigEditable(
        listOf(
            SceneConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            SceneElementConfigField("Parts", SceneConfig::parts, { c, n -> c.copy(parts = n) })
        )
    ) {
    @Serializable
    data class ScenePart(val lightId: Int, val brightness: Int, val hue: Int? = null, val saturation: Int? = null, val temperature: Int? = null)
}
