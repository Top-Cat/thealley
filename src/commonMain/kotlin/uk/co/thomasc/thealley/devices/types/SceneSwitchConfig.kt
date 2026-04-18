package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.zigbee.moes.SceneSwitchState

@Serializable
@SerialName("SceneSwitch")
data class SceneSwitchConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee",
    val targets0: List<Int>,
    val targets1: List<Int>,
    val targets2: List<Int>
) : IAlleyConfig<SceneSwitchState>,
    IZigbeeConfig<SceneSwitchState>,
    IConfigEditable<SceneSwitchConfig> by SimpleConfigEditable(
        listOf(
            SceneSwitchConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            SceneSwitchConfig::deviceId.fieldEditor("Device ID") { c, n -> c.copy(deviceId = n) },
            SceneSwitchConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) },
            SceneSwitchConfig::targets0.fieldEditor("Target Button 0") { c, n -> c.copy(targets0 = n) },
            SceneSwitchConfig::targets1.fieldEditor("Target Button 1") { c, n -> c.copy(targets1 = n) },
            SceneSwitchConfig::targets2.fieldEditor("Target Button 2") { c, n -> c.copy(targets2 = n) }
        )
    ) {

    override val defaultState = SceneSwitchState()
    override val stateSerializer = SceneSwitchState.serializer()
}
