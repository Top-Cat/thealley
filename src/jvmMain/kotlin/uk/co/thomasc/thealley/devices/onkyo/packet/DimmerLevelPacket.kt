package uk.co.thomasc.thealley.devices.onkyo.packet

data class DimmerLevelPacket(val command: Command = Command.Status) : IOnkyoResponse("DIM") {
    constructor(command: String) : this(Command.lookup[command] ?: Command.Dim)

    override fun toMessage() = command.cmd

    enum class Command(val cmd: String) {
        Bright("00"), Dim("01"), Dark("02"), ShutOff("03"),
        BrightLedOff("08"), Up("DIM"), Status(QUERY);
        companion object {
            val lookup = entries.associateBy { it.cmd }
        }
    }
}
