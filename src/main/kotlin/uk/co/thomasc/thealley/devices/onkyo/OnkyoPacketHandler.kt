package uk.co.thomasc.thealley.devices.onkyo

import uk.co.thomasc.thealley.devices.onkyo.packet.IOnkyoResponse

fun interface OnkyoPacketHandler<in T : IOnkyoResponse> {
    suspend fun invoke(event: T)
}
