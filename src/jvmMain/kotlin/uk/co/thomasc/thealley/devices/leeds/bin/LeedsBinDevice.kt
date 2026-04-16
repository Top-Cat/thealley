package uk.co.thomasc.thealley.devices.leeds.bin

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import mu.KLogging
import uk.co.thomasc.thealley.client
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.leeds.bin.LeedsBinState
import uk.co.thomasc.thealley.devices.system.TickEvent
import uk.co.thomasc.thealley.devices.types.LeedsBinConfig
import kotlin.time.Duration.Companion.days

@Serializable
data class LeedsBin(
    val type: BinType,
    val date: LocalDateTime
) {
    val day = date.date
}

enum class BinType {
    Black, Green, Brown
}

class LeedsBinDevice(id: Int, config: LeedsBinConfig, state: LeedsBinState, stateStore: IStateUpdater<LeedsBinState>) :
    AlleyDevice<LeedsBinDevice, LeedsBinConfig, LeedsBinState>(id, config, state, stateStore) {

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<TickEvent> { ev ->
            if (state.nextCatchup?.let { ev.now > it } != false) {
                val bins = client.get("https://api.leeds.gov.uk/public/waste/v1/BinsDays") {
                    contentType(ContentType.Application.Json)
                    header("Ocp-Apim-Subscription-Key", config.apiKey)
                    parameter("uprn", config.uprn)
                    parameter("startDate", ev.now.toLocalDateTime(TimeZone.UTC).date)
                    parameter("endDate", ev.now.plus(180.days).toLocalDateTime(TimeZone.UTC).date)
                }.body<List<LeedsBin>>()

                val byType = bins
                    .filter { it.date > ev.now.toLocalDateTime(TimeZone.UTC) }
                    .groupBy({ it.type }, { it.day })
                    .mapValues { it.value.min() }

                val nextBlack = byType[BinType.Black]
                val nextGreen = byType[BinType.Green]
                val nextBrown = byType[BinType.Brown]

                logger.debug { "Got leeds bin days, Black = $nextBlack, Green = $nextGreen, Brown = $nextBrown" }

                val next = bins.minOf { it.date.toInstant(TimeZone.UTC) }
                updateState(state.copy(nextCatchup = next, nextBlack = nextBlack, nextGreen = nextGreen, nextBrown = nextBrown))

                bus.emit(LeedsBinEvent(nextBlack, nextGreen, nextBrown))
            }
        }
    }

    companion object : KLogging()
}
