package uk.co.thomasc.thealley.devices.state

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.kasa.IKasaState
import uk.co.thomasc.thealley.devices.state.zigbee.IZigbeeState

@Serializable
object EmptyState : IZigbeeState, IKasaState, IAlleyState
