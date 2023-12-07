package uk.co.thomasc.thealley.devices.onkyo.packet

data class MutingPacket(val command: MutingCommand = MutingCommand.Status) : IOnkyoResponse("AMT") {
    constructor(command: String) : this(MutingCommand.lookup[command] ?: MutingCommand.Off)

    override fun toMessage() = command.cmd

    enum class MutingCommand(val cmd: String) {
        Off("00"), On("01"), Toggle("TG"), Status(QUERY);
        companion object {
            val lookup = entries.associateBy { it.cmd }
        }
    }
}
