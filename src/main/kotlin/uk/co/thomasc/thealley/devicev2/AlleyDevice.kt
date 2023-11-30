package uk.co.thomasc.thealley.devicev2

import uk.co.thomasc.thealley.devicev2.types.IAlleyConfig
import java.io.Closeable

abstract class AlleyDevice<A : AlleyDevice<A, T, U>, T : IAlleyConfig, U : Any>(val id: Int, val config: T, var state: U, private val stateStore: IStateUpdater<U>) : Closeable {
    open suspend fun init(bus: AlleyEventBus) { }
    suspend fun updateState(state: U) {
        if (this.state != state) {
            stateStore.saveState(state)
            this.state = state
        }
    }

    override fun close() {
        // Do nothing by default
    }
}
