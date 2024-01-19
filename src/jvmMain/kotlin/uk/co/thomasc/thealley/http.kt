package uk.co.thomasc.thealley

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

val alleyJsonUgly = Json

val alleyJson = Json(alleyJsonUgly) {
    prettyPrint = true
}

open class EnumAsIntSerializer<T : Enum<*>>(serialName: String, val serialize: (v: T) -> Int, val deserialize: (v: Int) -> T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeInt(serialize(value))
    }

    override fun deserialize(decoder: Decoder) =
        deserialize(decoder.decodeInt())
}

private fun setupClient(block: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {}) = HttpClient(Apache) {
    install(HttpTimeout)
    install(ContentNegotiation) {
        json(alleyJson)
    }

    engine {
        customizeClient {
            setMaxConnTotal(100)
            setMaxConnPerRoute(20)
        }
    }

    block()
}

val client = setupClient()
