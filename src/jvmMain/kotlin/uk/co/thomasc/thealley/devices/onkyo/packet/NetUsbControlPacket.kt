package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbControlPacket(val command: Command) : IOnkyoResponse("NTC") {
    constructor(command: String) : this(
        when (command) {
            Command.Play.cmd -> Command.Play
            Command.Pause.cmd -> Command.Pause
            Command.Stop.cmd -> Command.Stop
            else -> throw Exception("Unknown net usb command")
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Play : Command("PLAY")
        data object Pause : Command("PAUSE")
        data object Stop : Command("STOP")
        data object TrackUp : Command("TRUP")
        data object TrackDown : Command("TRDN")
        data object Repeat : Command("REPEAT")
        data object Random : Command("RANDOM")
    }
}
