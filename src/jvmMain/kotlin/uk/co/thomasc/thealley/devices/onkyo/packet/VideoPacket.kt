package uk.co.thomasc.thealley.devices.onkyo.packet

data class VideoPacket(val command: VideoCommand = VideoCommand.Status) : IOnkyoResponse("IFV") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> VideoCommand.Status
            else -> command.split(",").let {
                VideoCommand.Data(it[0], it[1], it[2], it[3], it[4], it[5], it[6], it[7], it[8], it[9])
            }
        }
    )

    override fun toMessage() = command.cmd

    sealed class VideoCommand(val cmd: String) {
        data object Status : VideoCommand(QUERY)
        data class Data(
            val inputSource: String,
            val inputFormat: String,
            val inputColorSpace: String,
            val inputColorDepth: String,
            val outputPort: String,
            val outputFormat: String,
            val outputColorSpace: String,
            val outputColorDepth: String,
            val unknown: String,
            val pictureMode: String
        ) : VideoCommand("")
    }
}
