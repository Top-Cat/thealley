package uk.co.thomasc.thealley.devices.onkyo.packet

data class AudioPacket(val command: AudioCommand = AudioCommand.Status) : IOnkyoResponse("IFA") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> AudioCommand.Status
            else -> command.split(",").let {
                AudioCommand.Data(it[0], it[1], it[2], it[3], it[4], it[5])
            }
        }
    )

    override fun toMessage() = command.cmd

    sealed class AudioCommand(val cmd: String) {
        data object Status : AudioCommand(QUERY)
        data class Data(
            val inputSource: String,
            val inputFormat: String,
            val inputFreq: String,
            val inputChannels: String,
            val outputMode: String,
            val outputChannels: String
        ) : AudioCommand("")
    }
}
