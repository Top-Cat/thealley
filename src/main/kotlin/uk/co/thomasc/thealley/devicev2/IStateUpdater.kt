package uk.co.thomasc.thealley.devicev2

interface IStateUpdater<U> {
    suspend fun saveState(state: U)
}
