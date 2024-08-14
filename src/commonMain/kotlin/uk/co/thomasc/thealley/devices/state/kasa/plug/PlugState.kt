package uk.co.thomasc.thealley.devices.state.kasa.plug

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.kasa.IKasaState

@Serializable
data class PlugState(val on: Boolean) : IKasaState
