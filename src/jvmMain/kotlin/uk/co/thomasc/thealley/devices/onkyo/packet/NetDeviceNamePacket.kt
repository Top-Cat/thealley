package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetDeviceNamePacket(val command: Command) : IOnkyoResponse("NDN") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> Command.Name(command)
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Name(val name: String) : Command(name)
    }
}
