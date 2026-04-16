package uk.co.thomasc.thealley.devices.leeds.bin

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.types.BinDisplayConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice

fun LocalDate?.toUnixAtMidnight() = (this?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds() ?: 0) / 1000

class BinDisplayDevice(id: Int, config: BinDisplayConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeDevice<BinDisplayUpdate, BinDisplayDevice, BinDisplayConfig, EmptyState>(id, config, state, stateStore, BinDisplayUpdate.serializer()) {

    override suspend fun onInit(bus: AlleyEventBusShim) {
        bus.handle<LeedsBinEvent> {
            setTimes(bus, it)
        }
    }

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: BinDisplayUpdate) {
        // Ignore
    }

    private suspend fun setTimes(bus: AlleyEventEmitter, event: LeedsBinEvent) =
        sendUpdate(
            bus,
            TimeUpdateCommand(
                BinDisplayTimes(
                    event.nextBlack.toUnixAtMidnight(),
                    event.nextGreen.toUnixAtMidnight(),
                    event.nextBrown.toUnixAtMidnight()
                )
            )
        )

    companion object : KLogging()
}
