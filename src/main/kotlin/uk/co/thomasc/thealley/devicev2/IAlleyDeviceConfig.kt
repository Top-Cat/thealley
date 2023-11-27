package uk.co.thomasc.thealley.devicev2

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class IAlleyDeviceConfig<T : AlleyDevice<T, U, V>, U : IAlleyConfig, V: Any> {
    fun generate(json: Json, stateUpdateFactory: StateUpdaterFactory, state: String): T {
        val serializer = stateSerializer()

        val stateObj = json.decodeFromString(serializer, state)
        val stateUpdater = stateUpdateFactory.getUpdater(serializer)

        return create(stateObj, stateUpdater)
    }
    abstract fun create(state: V, stateStore: IStateUpdater<V>): T
    abstract fun stateSerializer(): KSerializer<V>
}
