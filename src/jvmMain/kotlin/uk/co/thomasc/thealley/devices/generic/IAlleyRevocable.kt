package uk.co.thomasc.thealley.devices.generic

interface IAlleyRevocable {
    suspend fun hold()
    suspend fun revoke()
}
