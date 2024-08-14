package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.kasa.bulb.BulbDevice
import uk.co.thomasc.thealley.devices.state.kasa.bulb.BulbState

class BulbDeviceConfig(config: BulbConfig) : IAlleyDeviceConfig<BulbDevice, BulbConfig, BulbState>(config) {
    override fun create(id: Int, state: BulbState, stateStore: IStateUpdater<BulbState>, dev: AlleyDeviceMapper) = BulbDevice(id, config, state, stateStore)
}
