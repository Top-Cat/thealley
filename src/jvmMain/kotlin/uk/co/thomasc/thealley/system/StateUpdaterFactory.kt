package uk.co.thomasc.thealley.system

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.IAlleyState

class StateUpdaterFactory(private val json: Json, private val id: Int) {
    fun <U : IAlleyState> getUpdater(serializer: KSerializer<U>): IStateUpdater<U> = StateUpdaterImpl(json, serializer, id)
}
