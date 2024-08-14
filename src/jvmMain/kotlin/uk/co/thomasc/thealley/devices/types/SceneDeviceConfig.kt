package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.scene.SceneDevice
import uk.co.thomasc.thealley.devices.system.scene.SceneState

class SceneDeviceConfig(config: SceneConfig) : IAlleyDeviceConfig<SceneDevice, SceneConfig, SceneState>(config) {
    override fun create(id: Int, state: SceneState, stateStore: IStateUpdater<SceneState>, dev: AlleyDeviceMapper) = SceneDevice(id, config, state, stateStore, dev)
}
