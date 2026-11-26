package uk.co.thomasc.thealley.devices.onkyo

import kotlinx.coroutines.delay
import mu.KLogging
import uk.co.thomasc.thealley.devices.onkyo.packet.ArtPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.AudioPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.IOnkyoResponse
import uk.co.thomasc.thealley.devices.onkyo.packet.InputPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MasterVolumePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MutingPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.PowerPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.ReceiverInformationPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.VideoPacket
import kotlin.time.Duration.Companion.seconds

suspend fun getPowerStatus() {
    val logger = KLogging().logger
    val queries = listOf(
        PowerPacket(),
        MasterVolumePacket(),
        ReceiverInformationPacket()
    )

    discovery(5.seconds)?.firstOrNull()?.let { res ->
        logger.info("$res")

        OnkyoConnection(res.address.hostname).apply {
            init()

            await(5.seconds)

            queries.forEach { query ->
                when (val response = send<IOnkyoResponse>(query)) {
                    is MasterVolumePacket -> logger.info("Volume is ${response.command}")
                    is PowerPacket -> logger.info("Device is ${response.command}")
                    is MutingPacket -> logger.info("Muted? ${response.command}")
                    is AudioPacket -> logger.info("Audio? ${response.command}")
                    is VideoPacket -> logger.info("Video? ${response.command}")
                    is ArtPacket -> logger.info("Art? ${response.command}")
                    is InputPacket -> logger.info("Input? ${response.command}")
                    is ReceiverInformationPacket -> logger.info("XML! ${response.command}")
                    else -> logger.info("Wat")
                }
            }

            delay(10.seconds)

            send<MasterVolumePacket>(MasterVolumePacket(MasterVolumePacket.VolumeCommand.Level(72)))
            send<ReceiverInformationPacket>(ReceiverInformationPacket())
        }
    }
}
