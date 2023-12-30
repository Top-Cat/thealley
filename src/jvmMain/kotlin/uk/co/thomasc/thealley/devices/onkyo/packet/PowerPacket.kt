package uk.co.thomasc.thealley.devices.onkyo.packet

data class PowerPacket(val command: PowerCommand = PowerCommand.Status) : IOnkyoResponse("PWR") {
    constructor(command: String) : this(PowerCommand.lookup[command] ?: PowerCommand.Off)

    override fun toMessage() = command.cmd

    enum class PowerCommand(val cmd: String) {
        Off("00"), On("01"), Status(QUERY);
        companion object {
            val lookup = entries.associateBy { it.cmd }
        }
    }
}
