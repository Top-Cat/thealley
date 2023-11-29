package uk.co.thomasc.thealley.devicev2.kasa.bulb

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.kasa.IKasaState

@Serializable
data class BulbState(val on: Boolean) : IKasaState
