package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.system.scene.SceneDevice
import uk.co.thomasc.thealley.devicev2.system.scene.SceneState

@Serializable
@SerialName("Scene")
data class SceneConfig(override val name: String, val parts: List<ScenePart>) : IAlleyConfig {
    override fun deviceConfig() = SceneDeviceConfig(this)

    @Serializable
    class ScenePart(val lightId: Int, val brightness: Int, val hue: Int?, val saturation: Int?, val temperature: Int?)

    class SceneDeviceConfig(val config: SceneConfig) : IAlleyDeviceConfig<SceneDevice, SceneConfig, SceneState>() {
        override fun create(id: Int, state: SceneState, stateStore: IStateUpdater<SceneState>, dev: AlleyDeviceMapper) = SceneDevice(id, config, state, stateStore, dev)
        override fun stateSerializer() = SceneState.serializer()
    }
}
