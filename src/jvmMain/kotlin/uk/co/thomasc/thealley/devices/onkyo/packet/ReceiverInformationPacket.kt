package uk.co.thomasc.thealley.devices.onkyo.packet

import nl.adaptivity.xmlutil.serialization.XML
import uk.co.thomasc.thealley.devices.onkyo.info.ReceiverInformation

data class ReceiverInformationPacket(val command: Command = Command.Status) : IOnkyoResponse("NRI") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Status
            else -> Command.Data(XML.decodeFromString(command))
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Status : Command(QUERY)
        data class Data(
            val data: ReceiverInformation
        ) : Command("")
    }
}
