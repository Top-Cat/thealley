package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable

sealed interface IBlindState {
    @Serializable
    data class SingleDirection(val openPercent: Int) : IBlindState

    @Serializable
    data class MultipleDirections(val openState: List<DirectionInfo>) : IBlindState

    @Serializable
    data class DirectionInfo(val openPercent: Int, val openDirection: OpenCloseTrait.Direction)
}
