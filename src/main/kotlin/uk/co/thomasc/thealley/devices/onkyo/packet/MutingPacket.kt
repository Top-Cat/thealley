package uk.co.thomasc.thealley.devices.onkyo.packet

data class MutingPacket(val command: MutingCommand = MutingCommand.STATUS) : IOnkyoResponse("AMT") {
    constructor(command: String) : this(MutingCommand.lookup[command] ?: MutingCommand.OFF)

    override fun toMessage() = command.cmd

    enum class MutingCommand(val cmd: String) {
        OFF("00"), ON("01"), TOGGLE("TG"), STATUS(QUERY);
        companion object {
            val lookup = entries.associateBy { it.cmd }
        }
    }
}
