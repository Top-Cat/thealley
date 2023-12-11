package uk.co.thomasc.thealley.web.google

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

sealed interface ExecuteStatus {
    val name: String
    fun combine(other: ExecuteStatus): ExecuteStatus
    interface WithState : ExecuteStatus {
        val state: Map<String, JsonElement>
    }
    data class SUCCESS(override val state: Map<String, JsonElement> = mapOf()) : WithState {
        override val name = "SUCCESS"
        override fun combine(other: ExecuteStatus) = when (other) {
            is ERROR -> other
            OFFLINE -> other
            PENDING -> this
            is WithState -> SUCCESS(
                state.plus(other.state)
            )
        }
    }
    data object DEFAULT : WithState {
        override val state: Map<String, JsonElement> = mapOf("online" to JsonPrimitive(true))
        override val name
            get() = throw IllegalStateException("Can't return state as a status")
        override fun combine(other: ExecuteStatus) = other
    }
    data object PENDING : ExecuteStatus {
        override val name = "PENDING"
        override fun combine(other: ExecuteStatus) = other
    }
    data object OFFLINE : ExecuteStatus {
        override val name = "OFFLINE"
        override fun combine(other: ExecuteStatus) = if (other is ERROR) other else this
    }

    data class ERROR(val errorCode: GoogleHomeErrorCode) : ExecuteStatus {
        override val name = "ERROR"
        override fun combine(other: ExecuteStatus) = this
    }
}
