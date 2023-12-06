package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.StateUpdaterFactory

@Serializable
sealed class IAlleyDeviceConfig<T : AlleyDevice<T, U, V>, U : IAlleyConfig, V : Any> {
    fun generate(id: Int, json: Json, stateUpdateFactory: StateUpdaterFactory, state: String, dev: AlleyDeviceMapper): T {
        val serializer = stateSerializer()

        val stateObj = json.decodeFromString(serializer, state)
        val stateUpdater = stateUpdateFactory.getUpdater(serializer)

        return create(id, stateObj, stateUpdater, dev)
    }
    abstract fun create(id: Int, state: V, stateStore: IStateUpdater<V>, dev: AlleyDeviceMapper): T
    abstract fun stateSerializer(): KSerializer<V>
}
