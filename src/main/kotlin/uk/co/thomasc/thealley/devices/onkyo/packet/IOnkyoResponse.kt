package uk.co.thomasc.thealley.devices.onkyo.packet

import uk.co.thomasc.thealley.devices.onkyo.Packet

sealed class IOnkyoResponse(private val group: String) {
    protected abstract fun toMessage(): String
    private fun toBytes() = "!1$group${toMessage()}".toByteArray()
    fun toPacket() = Packet(toBytes())
    companion object {
        const val QUERY = "QSTN"
    }
}
