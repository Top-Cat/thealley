package uk.co.thomasc.thealley.devices.onkyo.packet

import kotlin.math.abs

data class SubwooferLevelPacket(val command: Command = Command.Status) : IOnkyoResponse("SWL") {
    constructor(command: String) : this(
        when (command) {
            Command.Up.cmd -> Command.Up
            Command.Down.cmd -> Command.Down
            QUERY -> Command.Status
            else -> Command.Level(command.toInt())
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Up : Command("UP")
        data object Down : Command("DOWN")
        data object Status : Command(QUERY)
        data class Level(val level: Int) : Command("%s%02d".format(getSign(level), abs(level)))
    }

    companion object {
        fun getSign(level: Int) = when {
            level > 0 -> "+"
            level < 0 -> "-"
            else -> "0"
        }
    }
}
