package uk.co.thomasc.thealley.devices.onkyo.packet

data class PowerPacket(val command: PowerCommand = PowerCommand.STATUS) : IOnkyoResponse("PWR") {
    constructor(command: String) : this(PowerCommand.lookup[command] ?: PowerCommand.OFF)

    override fun toMessage() = command.cmd

    enum class PowerCommand(val cmd: String) {
        OFF("00"), ON("01"), STATUS(QUERY);
        companion object {
            val lookup = entries.associateBy { it.cmd }
        }
    }
}
