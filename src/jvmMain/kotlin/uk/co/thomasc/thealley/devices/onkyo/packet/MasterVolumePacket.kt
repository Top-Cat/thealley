package uk.co.thomasc.thealley.devices.onkyo.packet

data class MasterVolumePacket(val command: VolumeCommand = VolumeCommand.Status) : IOnkyoResponse("MVL") {
    constructor(command: String) : this(
        when (command) {
            VolumeCommand.Up.cmd -> VolumeCommand.Up
            VolumeCommand.Up1.cmd -> VolumeCommand.Up1
            VolumeCommand.Down.cmd -> VolumeCommand.Down
            VolumeCommand.Down1.cmd -> VolumeCommand.Down1
            QUERY -> VolumeCommand.Status
            else -> VolumeCommand.Level(command.toInt(16))
        }
    )

    override fun toMessage() = command.cmd

    sealed class VolumeCommand(val cmd: String) {
        data object Up : VolumeCommand("UP")
        data object Up1 : VolumeCommand("UP1")
        data object Down : VolumeCommand("DOWN")
        data object Down1 : VolumeCommand("DOWN1")
        data object Status : VolumeCommand(QUERY)
        data class Level(val level: Int) : VolumeCommand(Integer.toHexString(level))
    }
}
