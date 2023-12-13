package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbTrackInfoPacket(val command: Command) : IOnkyoResponse("NTR") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> {
                val parts = command.split("/")
                Command.Info(parts[0], parts[1])
            }
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Info(val first: String, val second: String) : Command("$first/$second")
    }
}
