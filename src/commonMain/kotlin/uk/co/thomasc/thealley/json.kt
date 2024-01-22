package uk.co.thomasc.thealley

import kotlinx.serialization.json.Json

val alleyJsonUgly = Json

val alleyJson = Json(alleyJsonUgly) {
    prettyPrint = true
}
