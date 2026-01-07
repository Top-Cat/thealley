package uk.co.thomasc.thealley

import kotlinx.serialization.json.Json

val alleyJsonUgly = Json

val alleyJsonLenient = Json(alleyJsonUgly) {
    ignoreUnknownKeys = true
}

val alleyJson = Json(alleyJsonLenient) {
    prettyPrint = true
}
