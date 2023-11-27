package uk.co.thomasc.thealley.devicev2

abstract class AlleyDevice<A : AlleyDevice<A, T, U>, T : Any, U: Any>(val config: T, var state: U, private val stateStore: IStateUpdater<U>) {
    open suspend fun init(bus: AlleyEventBus) { }
    fun updateState(state: U) {
        stateStore.saveState(state)
        this.state = state
    }
}
