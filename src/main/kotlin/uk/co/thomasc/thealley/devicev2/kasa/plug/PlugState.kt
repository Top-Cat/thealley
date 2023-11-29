package uk.co.thomasc.thealley.devicev2.kasa.plug

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.kasa.IKasaState

@Serializable
data class PlugState(val on: Boolean) : IKasaState
