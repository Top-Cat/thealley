package uk.co.thomasc.thealley.devicev2

interface IStateUpdater<U> {
    fun saveState(state: U)
}
