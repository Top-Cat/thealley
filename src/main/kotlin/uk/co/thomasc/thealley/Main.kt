package uk.co.thomasc.thealley

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.mustachejava.DefaultMustacheFactory
import com.toxicbakery.bcrypt.Bcrypt
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.form
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.auth.principal
import io.ktor.client.request.forms.formData
import io.ktor.features.ContentNegotiation
import io.ktor.features.NotFoundException
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.mustache.Mustache
import io.ktor.mustache.MustacheContent
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.runBlocking
import nl.myndocs.oauth2.authenticator.Credentials
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.inmemory.InMemoryClient
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.request.KtorCallContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import uk.co.thomasc.thealley.client.KotlinTimeModule
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.client.TadoClient
import uk.co.thomasc.thealley.config.AlleyTokenStore
import uk.co.thomasc.thealley.config.clients
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.repo.SceneRepository
import uk.co.thomasc.thealley.repo.SwitchRepository
import uk.co.thomasc.thealley.repo.UserRepository
import uk.co.thomasc.thealley.rest.Api
import uk.co.thomasc.thealley.rest.apiRoute
import uk.co.thomasc.thealley.rest.controlRoute
import uk.co.thomasc.thealley.rest.externalRoute
import uk.co.thomasc.thealley.rest.statsRoute
import uk.co.thomasc.thealley.scenes.SceneController
import uk.co.thomasc.thealley.web.mainRoute
import javax.sql.DataSource
import kotlin.collections.Set
import kotlin.collections.emptyMap
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.setOf
import kotlin.collections.toSet

fun setupDB(): DataSource {
    val dbHost = System.getenv("MYSQL_HOSTNAME") ?: "localhost"
    val dbPort = System.getenv("MYSQL_PORT") ?: "3306"
    val dbUser = System.getenv("MYSQL_USER") ?: "thealley"
    val dbName = System.getenv("MYSQL_DB") ?: "thealley"
    val dbPass = System.getenv("MYSQL_PASSWORD") ?: "insecure-password"

    return HikariDataSource(
        HikariConfig().apply {
            poolName = "mysql-pool"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = "jdbc:mysql://$dbHost:$dbPort/$dbName?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false"
            username = dbUser
            password = dbPass
            minimumIdle = 2
            idleTimeout = 10000
            maximumPoolSize = 20
            connectionTestQuery = "SELECT 1"
        }
    ).also {
        Database.connect(it)
    }
}

fun Application.setup() {
    setupDB().let { ds ->
        Flyway.configure()
            .dataSource(ds)
            .locations("db/migration")
            .load()
            .migrate()
    }

    val config = config()
    val clients = clients()

    val connectionOptions = MqttConnectOptions().apply {
        serverURIs = arrayOf("tcp://${config.mqtt.host}:1883")
        userName = config.mqtt.user
        password = config.mqtt.pass.toCharArray()
        maxInflight = 50
        isAutomaticReconnect = true
    }

    val client = MqttClient("tcp://${config.mqtt.host}:1883", config.mqtt.clientId)

    val sender = object : RelayMqtt.DeviceGateway {
        override fun sendToMqtt(topic: String, payload: MqttMessage) {
            client.publish(topic, payload)
        }
    }
    val relayClient = RelayClient(config, sender)

    val api = Api()
    val switchRepository = SwitchRepository()
    val userRepository = UserRepository()
    val deviceMapper = DeviceMapper(relayClient, switchRepository)
    val sr = SceneRepository(deviceMapper)

    val sceneController = SceneController(sr, switchRepository)
    val mqtt = RelayMqtt(client, relayClient, sceneController, api)
    val ss = SwitchServer(sceneController, sender)
    val tado = TadoClient(config)

    val alleyTokenStore = AlleyTokenStore()
    client.connect(connectionOptions)

    install(ContentNegotiation) {
        formData { }
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerModule(JavaTimeModule())
            registerModule(KotlinTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    install(Locations)
    install(StatusPages) {
        exception<NotFoundException> {
            call.respond(HttpStatusCode.NotFound, "Not Found")
        }

        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            throw cause
        }
    }

    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }

    install(Authentication) {
        form("oauth-login") {
            userParamName = "username"
            validate { creds ->
                userRepository.getUserByName(creds.name)?.let {
                    if (Bcrypt.verify(creds.password, it.password.removePrefix("{bcrypt}").toByteArray())) {
                        UserIdPrincipal(it.username)
                    } else {
                        null
                    }
                }
            }
        }
    }

    install(Sessions) {
        cookie<UserIdPrincipal>("SESSION_ID", SessionStorageMemory()) {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Oauth2ServerFeature) {
        authenticationCallback = { call, callRouter ->
            runBlocking {
                val context = KtorCallContext(call)

                val userSession = call.sessions.get<UserIdPrincipal>()

                if (userSession == null) {
                    context.applicationCall.respond(
                        MustacheContent("login.mustache", null)
                    )
                } else {
                    callRouter.route(context, Credentials(userSession.name, "")).also { response ->
                        if (!response.successfulLogin) {
                            // Clear auth, we can't show login again now
                            call.sessions.clear("SESSION_ID")
                        }
                    }
                }
            }
        }

        tokenEndpoint = "/external/oauth/token"
        authorizationEndpoint = "/external/oauth/authorize"
        tokenInfoEndpoint = "/external/oauth/tokeninfo"

        identityService = object : IdentityService {
            override fun allowedScopes(forClient: Client, identity: Identity, scopes: Set<String>) = scopes

            override fun identityOf(forClient: Client, username: String) =
                userRepository.getUserByName(username)?.let {
                    Identity(
                        it.username,
                        emptyMap()
                    )
                }

            override fun validCredentials(forClient: Client, identity: Identity, password: String) =
                userRepository.getUserByName(identity.username)?.let {
                    true // Already validated
                } ?: false
        }

        clientService = InMemoryClient().also { imc ->
            clients.clients.forEach {
                imc.client {
                    clientId = it.clientId
                    clientSecret = it.secret
                    scopes = it.scopes.toSet()
                    authorizedGrantTypes = setOf(
                        AuthorizedGrantType.AUTHORIZATION_CODE,
                        AuthorizedGrantType.REFRESH_TOKEN
                    )
                    redirectUris = setOf(
                        "https://developers.google.com/oauthplayground",
                        "https://oauth-redirect.googleusercontent.com/r/the-alley-4c2e7"
                    )
                }
            }
        }

        tokenStore = alleyTokenStore
    }

    routing {
        apiRoute(api, sceneController)
        statsRoute(switchRepository, tado, deviceMapper, relayClient)
        controlRoute(switchRepository, sceneController, deviceMapper)
        mainRoute(switchRepository)

        authenticate("oauth-login") {
            post("/external/login") {
                call.sessions.set(call.principal<UserIdPrincipal>())
                call.request.header(HttpHeaders.Referrer)?.let { call.respondRedirect(it) } ?: run {
                    call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
                }
            }
        }

        externalRoute(switchRepository, sceneController, alleyTokenStore, deviceMapper)

        static("") {
            resources("static")
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkOauth(alleyTokenStore: AlleyTokenStore, block: suspend () -> Unit) {
    val authHeader = call.request.parseAuthorizationHeader()
    if (authHeader is HttpAuthHeader.Single) {
        val token = alleyTokenStore.accessToken(authHeader.blob)

        when (token?.expired()) {
            true -> alleyTokenStore.revokeAccessToken(token.accessToken).let { null }
            false -> block().let { true }
            null -> null
        }
    } else {
        null
    } ?: call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
}

fun main(args: Array<String>) {
    EngineMain.main(args)
}
