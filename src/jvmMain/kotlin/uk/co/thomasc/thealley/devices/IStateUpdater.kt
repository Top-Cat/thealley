package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.state.IAlleyState

interface IStateUpdater<U : IAlleyState> {
    suspend fun saveState(newState: U)
    suspend fun encoded(state: U): String
}
