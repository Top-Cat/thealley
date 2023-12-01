package uk.co.thomasc.thealley.devicev2

import uk.co.thomasc.thealley.devicev2.types.IAlleyConfig
import java.io.Closeable

abstract class AlleyDevice<A : AlleyDevice<A, T, U>, T : IAlleyConfig, U : Any>(val id: Int, val config: T, state: U, private val stateStore: IStateUpdater<U>) : Closeable {
    private var currentState = state
    protected var state
        get() = currentState
        private set(value) {}

    open suspend fun init(bus: AlleyEventBus) { }
    suspend fun updateState(state: U) {
        if (this.currentState != state) {
            stateStore.saveState(state)
            this.currentState = state
        }
    }

    override fun close() {
        // Do nothing by default
    }
}
