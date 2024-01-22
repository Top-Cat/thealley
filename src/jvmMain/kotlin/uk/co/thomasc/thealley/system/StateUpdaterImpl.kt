package uk.co.thomasc.thealley.system

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import mu.KLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.repo.DeviceTable
import uk.co.thomasc.thealley.repo.NowExpression

internal class StateUpdaterImpl<U>(val json: Json, val serializer: KSerializer<U>, val id: Int) : IStateUpdater<U> {
    override suspend fun saveState(newState: U) {
        val localId = id
        val encoded = json.encodeToString(serializer, newState)
        logger.info { "Update state for $localId - $encoded" }

        newSuspendedTransaction {
            DeviceTable.update({
                DeviceTable.id eq localId
            }) {
                it[DeviceTable.state] = encoded
                it[DeviceTable.updatedAt] = NowExpression(DeviceTable.updatedAt)
            }
        }
    }

    companion object : KLogging()
}
