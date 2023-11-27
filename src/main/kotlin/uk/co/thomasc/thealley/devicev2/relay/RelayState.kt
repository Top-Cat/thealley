package uk.co.thomasc.thealley.devicev2.relay

import kotlinx.serialization.Serializable

@Serializable
data class RelayState(val on: Boolean)
