package uk.co.thomasc.thealley.devicev2

import uk.co.thomasc.thealley.devicev2.types.IAlleyConfig

abstract class AlleyDevice<A : AlleyDevice<A, T, U>, T : IAlleyConfig, U : Any>(val id: Int, val config: T, var state: U, private val stateStore: IStateUpdater<U>) {
    open suspend fun init(bus: AlleyEventBus) { }
    suspend fun updateState(state: U) {
        if (this.state != state) {
            stateStore.saveState(state)
            this.state = state
        }
    }
}
