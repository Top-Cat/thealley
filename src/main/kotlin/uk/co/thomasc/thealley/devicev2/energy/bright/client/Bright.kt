package uk.co.thomasc.thealley.devicev2.energy.bright.client

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import mu.KLogging
import uk.co.thomasc.thealley.client.client
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Bright(private val email: String, private val pass: String) {
    private val tokenMutex = Mutex()
    private var token: BrightToken? = null

    private suspend fun getToken() =
        client.post("https://api.glowmarkt.com/api/v0-1/auth") {
            contentType(ContentType.Application.Json)
            header("applicationId", "b0f1b774-a586-4f72-9edd-27ead8aa7a8d")
            setBody(BrightCredentials(email, pass))
        }.body<BrightToken>()

    private suspend fun tokenForGet(): String {
        tokenMutex.withLock {
            if (token?.let { t -> t.exp < Clock.System.now().epochSeconds } != false) {
                logger.info { "Getting token with login" }
                token = getToken()
            }
        }

        return token!!.token
    }

    private val resources = GlobalScope.async(start = CoroutineStart.LAZY) {
        client.get("https://api.glowmarkt.com/api/v0-1/virtualentity") {
            contentType(ContentType.Application.Json)
            header("applicationId", "b0f1b774-a586-4f72-9edd-27ead8aa7a8d")
            header("token", tokenForGet())
        }.body<List<BrightVirtualEntity>>().flatMap {
            it.resources
        }
    }

    private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    suspend fun catchup(type: BrightResourceType): BrightResourceCatchup {
        val resourceId = resources.await().first { it.type == type }.resourceId

        return client.get("https://api.glowmarkt.com/api/v0-1/resource/$resourceId/catchup") {
            contentType(ContentType.Application.Json)
            header("applicationId", "b0f1b774-a586-4f72-9edd-27ead8aa7a8d")
            header("token", tokenForGet())
        }.body<BrightResourceCatchup>()
    }

    suspend fun getReadings(type: BrightResourceType, period: BrightPeriod = BrightPeriod.PT30M, from: Instant? = null, to: Instant? = null): BrightResourceReadings {
        val resourceId = resources.await().first { it.type == type }.resourceId
        val fromLocal = from?.toLocalDateTime(TimeZone.UTC)?.toJavaLocalDateTime()
            ?: LocalDateTime.now().withSecond(1).withMinute(0).withHour(0)
        val toLocal = to?.toLocalDateTime(TimeZone.UTC)?.toJavaLocalDateTime()
            ?: LocalDateTime.now().withSecond(59).withMinute(59).withHour(23)

        return client.get("https://api.glowmarkt.com/api/v0-1/resource/$resourceId/readings") {
            contentType(ContentType.Application.Json)
            header("applicationId", "b0f1b774-a586-4f72-9edd-27ead8aa7a8d")
            header("token", tokenForGet())
            parameter("from", fromLocal.format(format))
            parameter("to", toLocal.format(format))
            parameter("period", period)
            parameter("function", BrightFunction.SUM)
            parameter("nulls", 1)
        }.body<BrightResourceReadings>()
    }

    companion object : KLogging()
}
