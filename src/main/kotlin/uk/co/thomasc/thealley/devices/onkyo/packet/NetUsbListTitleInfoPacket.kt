package uk.co.thomasc.thealley.devices.onkyo.packet

/*NET/USB List Info
NET/USB List Title Info
xx : Service Type
 00 : DLNA, 01 : Favorite, 02 : vTuner, 03 : SiriusXM, 04 : Pandora, 05 : Rhapsody, 06 : Last.fm,
 07 : Napster, 08 : Slacker, 09 : Mediafly, 0A : Spotify, 0B : AUPEO!, 0C : radiko, 0D : e-onkyo,
 0E : TuneIn Radio, 0F : MP3tunes, 10 : Simfy, 11:Home Media, 12:Deezer, 13:iHeartRadio,
 F0 : USB Front, F1 : USB Rear, F2 : Internet Radio, F3 : NET, FF : None
u : UI Type
 0 : List, 1 : Menu, 2 : Playback, 3 : Popup, 4 : Keyboard, "5" : Menu List
y : Layer Info
 0 : NET TOP, 1 : Service Top,DLNA/USB/iPod Top, 2 : under 2nd Layer
cccc : Current Cursor Position (HEX 4 letters)
iiii : Number of List Items (HEX 4 letters)
ll : Number of Layer(HEX 2 letters)
rr : Reserved (2 leters)
aa : Icon on Left of Title Bar
 00 : Internet Radio, 01 : Server, 02 : USB, 03 : iPod, 04 : DLNA, 05 : WiFi, 06 : Favorite
 10 : Account(Spotify), 11 : Album(Spotify), 12 : Playlist(Spotify), 13 : Playlist-C(Spotify)
 14 : Starred(Spotify), 15 : What's New(Spotify), 16 : Track(Spotify), 17 : Artist(Spotify)
 18 : Play(Spotify), 19 : Search(Spotify), 1A : Folder(Spotify)
 FF : None
bb : Icon on Right of Title Bar
 00 : DLNA, 01 : Favorite, 02 : vTuner, 03 : SiriusXM, 04 : Pandora, 05 : Rhapsody, 06 : Last.fm,
 07 : Napster, 08 : Slacker, 09 : Mediafly, 0A : Spotify, 0B : AUPEO!, 0C : radiko, 0D : e-onkyo,
 0E : TuneIn Radio, 0F : MP3tunes, 10 : Simfy, 11:Home Media, 12:Deezer, 13:iHeartRadio,
 FF : None
ss : Status Info
 00 : None, 01 : Connecting, 02 : Acquiring License, 03 : Buffering
 04 : Cannot Play, 05 : Searching, 06 : Profile update, 07 : Operation disabled
 08 : Server Start-up, 09 : Song rated as Favorite, 0A : Song banned from station,
 0B : Authentication Failed, 0C : Spotify Paused(max 1 device), 0D : Track Not Available, 0E : Cannot Skip
nnn...nnn : Character of Title Bar (variable-length, 64 Unicode letters [UTF-8 encoded] max)
 */
data class NetUsbListTitleInfoPacket(val command: Command) : IOnkyoResponse("NLT") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> Command.Info(
                NetUsbService.fromString(command.substring(0, 2)),
                command[2],
                command[3],
                command.substring(4, 8),
                command.substring(8, 12),
                command.substring(12, 14),
                command[14],
                command[15],
                command.substring(16, 18),
                command.substring(18, 20),
                command.substring(20, 22),
                if (command.length > 22) command.substring(23) else ""
            )
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Info(
            val service: NetUsbService,
            val uiType: Char,
            val layerInfo: Char,
            val cursorPosition: String,
            val listItems: String,
            val layer: String,
            val startFlag: Char,
            val reserved: Char,
            val iconLeft: String,
            val iconRight: String,
            val status: String,
            val rest: String
        ) : Command("")
    }
}
