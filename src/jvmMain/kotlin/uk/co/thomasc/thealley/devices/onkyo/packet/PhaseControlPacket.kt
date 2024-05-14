package uk.co.thomasc.thealley.devices.onkyo.packet

data class PhaseControlPacket(val command: Command = Command.Status) : IOnkyoResponse("PCT") {
    constructor(command: String) : this(Command.lookup[command] ?: Command.Off)

    override fun toMessage() = command.cmd

    enum class Command(val cmd: String) {
        Off("00"), On("01"), FullBandOn("02"), Up("UP"), Status(QUERY);
        companion object {
            val lookup = entries.associateBy { it.cmd }
        }
    }
}
