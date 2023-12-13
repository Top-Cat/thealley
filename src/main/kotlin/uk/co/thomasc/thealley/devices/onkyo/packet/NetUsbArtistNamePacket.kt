package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbArtistNamePacket(val command: Command) : IOnkyoResponse("NAT") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> Command.Artist(command)
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Artist(val artist: String) : Command(artist)
    }
}
