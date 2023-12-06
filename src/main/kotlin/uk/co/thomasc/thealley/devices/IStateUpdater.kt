package uk.co.thomasc.thealley.devices

interface IStateUpdater<U> {
    suspend fun saveState(state: U)
}
