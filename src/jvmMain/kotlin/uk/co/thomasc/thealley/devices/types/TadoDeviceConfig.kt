package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.energy.tado.TadoDevice
import uk.co.thomasc.thealley.devices.energy.tado.TadoState

class TadoDeviceConfig(val config: TadoConfig) : IAlleyDeviceConfig<TadoDevice, TadoConfig, TadoState>() {
    override fun create(id: Int, state: TadoState, stateStore: IStateUpdater<TadoState>, dev: AlleyDeviceMapper) = TadoDevice(id, config, state, stateStore)
    override fun stateSerializer() = TadoState.serializer()
}
