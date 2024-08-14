package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.switch.SwitchState

@Serializable
@SerialName("Switch")
data class SwitchConfig(
    override val name: String,
    val id: Int,
    val scenes: List<Int>
) : IAlleyConfig<SwitchState>,
    IConfigEditable<SwitchConfig> by SimpleConfigEditable(
        listOf(
            SwitchConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            SwitchConfig::id.fieldEditor("Switch Id") { c, n -> c.copy(id = n) },
            SwitchConfig::scenes.fieldEditor("Scenes", { it is SceneConfig }) { c, n -> c.copy(scenes = n) }
        )
    ) {
    override val defaultState = SwitchState()
    override val stateSerializer = SwitchState.serializer()
}
