package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbMenuStatusPacket(val command: Command) : IOnkyoResponse("NMS") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> {
                Command.Status(
                    command[0] == 'M',
                    OnkyoFunction.fromString(command.substring(1, 3)),
                    OnkyoFunction.fromString(command.substring(3, 5)),
                    command[5] == 'S',
                    TimeDisplayType.fromChar(command[6]),
                    NetUsbService.fromString(command.substring(7, 9))
                )
            }
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Status(
            val trackMenu: Boolean,
            val f1Func: OnkyoFunction,
            val f2Func: OnkyoFunction,
            val canSeek: Boolean,
            val timeDisplay: TimeDisplayType,
            val service: NetUsbService
        ) : Command("${if (trackMenu) "M" else "x"}${f1Func.encoding}${f2Func.encoding}${if (canSeek) "S" else "x"}${timeDisplay.encoding}${service.encoding}")
    }
}

enum class TimeDisplayType(val encoding: Char) {
    DISABLED('x'),
    ELAPSED_TOTAL('1'),
    ELAPSED('2');

    companion object {
        private val lookup = entries.associateBy { it.encoding }
        fun fromChar(c: Char) = lookup[c] ?: DISABLED
    }
}

enum class OnkyoFunction(val encoding: String) {
    DISABLED("xx"),
    LIKE("01"),
    DISLIKE("02"),
    LOVE("03"),
    BAN("04"),
    EPISODE("05"),
    RATINGS("06"),
    BAN_BLK("07"),
    BAN_WHT("08"),
    FAVOURITE_BLK("09"),
    FAVOURITE_WHT("0a"),
    FAVOURITE_YEL("0b");

    companion object {
        private val lookup = entries.associateBy { it.encoding }
        fun fromString(c: String) = lookup[c] ?: DISABLED
    }
}

enum class NetUsbService(val displayName: String, val encoding: String) {
    DLNA("Music Service (DLNA)", "00"),
    FAVOURITE("MY Favourite", "01"),
    VTUNER("vTuner", "02"),
    SIRIUSXM("SiriusXM", "03"),
    PANDORA("Pandora", "04"),
    RHAPSODY("Rhapsody", "05"),
    LASTFM("Last.fm", "06"),
    NAPSTER("Napster", "07"),
    SLACKER("Slacker", "08"),
    MEDIAFLY("Mediafly", "09"),
    SPOTIFY("Spotify", "0a"),
    AUPEO("AUPEO!", "0b"),
    RADIKO("radiko", "0c"),
    EONKYO("e-onkyo", "0d"),
    TUNEIN("TuneIn", "0e"),
    MP3TUNES("MP3tunes", "0f"),
    SIMFY("Simfy", "10"),
    HOME("Home Media", "11"),
    DEEZER("Deezer", "12"),
    IHEARTRADIO("iHeartRadio", "13"),
    AIRPLAY("AirPlay", "18"),
    ONKYOMUSIC("onkyo Music", "1a"),
    TIDAL("TIDAL", "1b"),
    USB0("USB Front", "f0"),
    USB1("USB Rear", "f1"),
    INTERNET("Internet Radio", "f2"),
    NET("NET", "f3"),
    BLUETOOTH("BLUETOOTH", "f4");

    companion object {
        private val lookup = entries.associateBy { it.encoding }
        fun fromString(c: String) = lookup[c.lowercase()] ?: NET
    }
}
