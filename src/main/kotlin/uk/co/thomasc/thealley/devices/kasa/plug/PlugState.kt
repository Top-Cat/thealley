package uk.co.thomasc.thealley.devices.kasa.plug

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.kasa.IKasaState

@Serializable
data class PlugState(val on: Boolean) : IKasaState
