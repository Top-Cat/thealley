package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbAlbumNamePacket(val command: Command) : IOnkyoResponse("NAL") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> Command.Album(command)
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Album(val album: String) : Command(album)
    }
}
