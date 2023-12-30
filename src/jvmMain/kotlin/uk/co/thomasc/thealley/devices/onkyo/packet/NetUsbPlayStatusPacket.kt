package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbPlayStatusPacket(val command: Command) : IOnkyoResponse("NST") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> Command.Info(
                Command.Info.PlayStatus.fromChar(command[0]),
                Command.Info.RepeatStatus.fromChar(command[1]),
                Command.Info.ShuffleStatus.fromChar(command[2])
            )
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Info(
            val playStatus: PlayStatus,
            val repeatStatus: RepeatStatus,
            val shuffleStatus: ShuffleStatus
        ) : Command("${playStatus.encoding}${repeatStatus.encoding}${shuffleStatus.encoding}") {
            enum class PlayStatus(val encoding: Char) {
                STOP('S'), PLAY('P'), PAUSE('p'), FAST_FORWARD('F'), REWIND('R'), EOF('E');

                companion object {
                    private val lookup = entries.associateBy { it.encoding }
                    fun fromChar(c: Char) = lookup[c] ?: EOF
                }
            }

            enum class RepeatStatus(val encoding: Char) {
                OFF('-'), ALL('R'), FOLDER('F'), REPEAT('1'), DISABLE('x');

                companion object {
                    private val lookup = entries.associateBy { it.encoding }
                    fun fromChar(c: Char) = lookup[c] ?: DISABLE
                }
            }

            enum class ShuffleStatus(val encoding: Char) {
                OFF('-'), ALL('S'), ALBUM('A'), FOLDER('F'), DISABLE('x');

                companion object {
                    private val lookup = entries.associateBy { it.encoding }
                    fun fromChar(c: Char) = lookup[c] ?: DISABLE
                }
            }
        }
    }
}
