package uk.co.thomasc.thealley.devices.onkyo.packet

data class InputPacket(val command: InputCommand = InputCommand.Status) : IOnkyoResponse("SLI") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> InputCommand.Status
            else -> InputCommand.Input(command)
        }
    )

    override fun toMessage() = command.cmd

    sealed class InputCommand(val cmd: String) {
        data object Status : InputCommand(QUERY)
        data object Up : InputCommand("UP")
        data object Down : InputCommand("DOWN")
        data class Input(val id: String) : InputCommand(id)
        // 12 -> ARC
        // 2E -> Bluetooth
    }
}
