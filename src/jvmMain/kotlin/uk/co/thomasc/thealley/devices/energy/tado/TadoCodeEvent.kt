package uk.co.thomasc.thealley.devices.energy.tado

import at.topc.tado.client.DeviceCodeResponse
import uk.co.thomasc.thealley.devices.system.IAlleyEvent

data class TadoCodeEvent(
    val account: String,
    val response: DeviceCodeResponse
) : IAlleyEvent
