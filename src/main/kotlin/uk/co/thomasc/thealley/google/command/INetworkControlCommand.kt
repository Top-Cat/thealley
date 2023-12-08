package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.Serializable

@Serializable
sealed interface INetworkControlCommand<out T : Any> : IGoogleHomeCommand<T> {
    override val params: T
}
