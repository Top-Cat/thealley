package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbTitleNamePacket(val command: Command) : IOnkyoResponse("NTI") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> Command.Title(command)
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Title(val title: String) : Command(title)
    }
}
