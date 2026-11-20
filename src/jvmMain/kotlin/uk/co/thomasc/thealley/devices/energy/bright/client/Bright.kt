package uk.co.thomasc.thealley.devices.energy.bright.client

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import mu.KLogging
import uk.co.thomasc.thealley.client
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    private suspend fun fetchResources() =
        client.get("https://api.glowmarkt.com/api/v0-1/virtualentity") {
            contentType(ContentType.Application.Json)
            header("applicationId", "b0f1b774-a586-4f72-9edd-27ead8aa7a8d")
            header("token", tokenForGet())
        }.body<List<BrightVirtualEntity>>().flatMap {
            it.resources
        }

    private suspend fun resourceId(type: BrightResourceType) =
        getResources()?.firstOrNull { it.type == type }?.resourceId

    private var resources: List<BrightResource>? = null
    private var lastResourceFailure: Instant? = null
    private val resourceTimeout = 10.seconds
    private val resourceRetryDelay = 5.minutes
    private val resourcesMutex = Mutex()

    private suspend fun getResources(): List<BrightResource>? {
        resources?.let { return it }

        return resourcesMutex.withLock {
            resources?.let { return@withLock it } // someone else may have loaded it while we waited

            val now = Clock.System.now()
            val lastFailure = lastResourceFailure
            if (lastFailure != null && now - lastFailure < resourceRetryDelay) {
                return@withLock null // API recently failed, don't hammer it or make callers wait
            }

            try {
                withTimeout(resourceTimeout) { fetchResources() }.also {
                    resources = it
                    lastResourceFailure = null
                }
            } catch (_: TimeoutCancellationException) {
                logger.warn { "Timed out fetching bright resources" }
                lastResourceFailure = now
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn { "Error fetching bright resources (${e.message})" }
                lastResourceFailure = now
                null
            }
        }
    }

    private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    suspend fun catchup(type: BrightResourceType): BrightResourceCatchup? {
        val resourceId = resourceId(type) ?: return null

        return try {
            client.get("https://api.glowmarkt.com/api/v0-1/resource/$resourceId/catchup") {
                contentType(ContentType.Application.Json)
                header("applicationId", "b0f1b774-a586-4f72-9edd-27ead8aa7a8d")
                header("token", tokenForGet())
            }.body<BrightResourceCatchup>()
        } catch (e: Exception) {
            logger.warn { "Error starting bright catchup (${e.message})" }
            null
        }
    }

    suspend fun getReadings(type: BrightResourceType, period: BrightPeriod = BrightPeriod.PT30M, from: Instant? = null, to: Instant? = null): BrightResourceReadings? {
        val resourceId = resourceId(type) ?: return null
        val fromLocal = from?.toLocalDateTime(TimeZone.UTC)?.toJavaLocalDateTime()
            ?: LocalDateTime.now().withSecond(1).withMinute(0).withHour(0)
        val toLocal = to?.toLocalDateTime(TimeZone.UTC)?.toJavaLocalDateTime()
            ?: LocalDateTime.now().withSecond(59).withMinute(59).withHour(23)

        return try {
            client.get("https://api.glowmarkt.com/api/v0-1/resource/$resourceId/readings") {
                contentType(ContentType.Application.Json)
                header("applicationId", "b0f1b774-a586-4f72-9edd-27ead8aa7a8d")
                header("token", tokenForGet())
                parameter("from", fromLocal.format(format))
                parameter("to", toLocal.format(format))
                parameter("period", period)
                parameter("function", BrightFunction.SUM)
                parameter("nulls", 1)
            }.body<BrightResourceReadings>()
        } catch (e: Exception) {
            logger.warn { "Error getting bright readings (${e.message})" }
            null
        }
    }

    companion object : KLogging()
}
