package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import java.time.Instant

val jackson: ObjectMapper = jacksonObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerModule(KotlinTimeModule())
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

class KotlinTimeModule : SimpleModule() {

    init {
        addSerializer(Instant::class.java, InstantSerializer.INSTANCE)
        addSerializer(Float::class.java, FloatSerializer.INSTANCE)
    }
}

class InstantSerializer : StdSerializer<Instant>(Instant::class.java) {
    companion object {
        val INSTANCE: InstantSerializer = InstantSerializer()
    }

    override fun serialize(value: Instant?, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

class FloatSerializer : StdSerializer<Float>(Float::class.java) {
    companion object {
        val INSTANCE: FloatSerializer = FloatSerializer()
    }

    override fun serialize(value: Float, gen: JsonGenerator, provider: SerializerProvider) {
        if (value % 1 == 0f) {
            gen.writeNumber(value.toInt())
        } else {
            gen.writeNumber(value)
        }
    }
}

private fun setupClient(block: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {}) = HttpClient(Apache) {
    install(HttpTimeout)
    install(JsonFeature) {
        serializer = JacksonSerializer(jackson)
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
