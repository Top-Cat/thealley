package uk.co.thomasc.thealley.devices

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.kasa.IKasaState
import uk.co.thomasc.thealley.devices.zigbee.IZigbeeState

@Serializable
object EmptyState : IZigbeeState, IKasaState
