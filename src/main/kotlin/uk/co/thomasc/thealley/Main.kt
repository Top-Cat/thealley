package uk.co.thomasc.thealley

import com.github.mustachejava.DefaultMustacheFactory
import com.toxicbakery.bcrypt.Bcrypt
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.request.forms.formData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.form
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.auth.principal
import io.ktor.server.http.content.staticResources
import io.ktor.server.locations.Locations
import io.ktor.server.mustache.Mustache
import io.ktor.server.mustache.MustacheContent
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionStorageMemory
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
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
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import uk.co.thomasc.thealley.oauth.AlleyTokenStore
import uk.co.thomasc.thealley.devices.newDevices
import uk.co.thomasc.thealley.oauth.ClientProperties
import uk.co.thomasc.thealley.repo.UserRepository
import uk.co.thomasc.thealley.web.ApiRoute
import uk.co.thomasc.thealley.web.ControlRoute
import uk.co.thomasc.thealley.web.ExternalRoute
import uk.co.thomasc.thealley.web.MainRoute
import uk.co.thomasc.thealley.web.StatsRoute
import javax.sql.DataSource
import kotlin.collections.set

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
    val (bus, devices) = newDevices()
    val userRepository = UserRepository()

    install(ContentNegotiation) {
        formData { }
        json(alleyJson)
    }

    install(Locations)
    install(StatusPages) {
        exception<NotFoundException> { call, _ ->
            call.respond(HttpStatusCode.NotFound, "Not Found")
        }

        exception<Throwable> { call, cause ->
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

    val alleyClientSvc = InMemoryClient().also { imc ->
        val clients = ClientProperties.fromApplication(this)
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
    val alleyTokenStore = AlleyTokenStore(alleyClientSvc)

    install(Oauth2ServerFeature) {
        authenticationCallback = { call, callRouter ->
            val context = KtorCallContext(call)

            val userSession = call.sessions.get<UserIdPrincipal>()

            if (userSession == null) {
                runBlocking {
                    context.applicationCall.respond(
                        MustacheContent("login.mustache", null)
                    )
                }
            } else {
                callRouter.route(context, Credentials(userSession.name, "")).also { response ->
                    if (!response.successfulLogin) {
                        // Clear auth, we can't show login again now
                        call.sessions.clear("SESSION_ID")
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

        clientService = alleyClientSvc

        tokenStore = alleyTokenStore
    }

    routing {
        val routes = listOf(
            ApiRoute(),
            StatsRoute(),
            ControlRoute(),
            MainRoute(),
            ExternalRoute(alleyTokenStore)
        )

        authenticate("oauth-login") {
            post("/external/login") {
                call.sessions.set(call.principal<UserIdPrincipal>())
                call.request.header(HttpHeaders.Referrer)?.let { call.respondRedirect(it) } ?: run {
                    call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
                }
            }
        }

        routes.forEach {
            with(it) {
                setup(bus, devices)
            }
        }

        staticResources("/static", "static")
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkOauth(alleyTokenStore: AlleyTokenStore, block: suspend (String) -> Unit) {
    val authHeader = call.request.parseAuthorizationHeader()
    if (authHeader is HttpAuthHeader.Single) {
        val token = alleyTokenStore.accessToken(authHeader.blob)

        when (token?.expired()) {
            true -> alleyTokenStore.revokeAccessToken(token.accessToken).let { null }
            false -> block(token.identity?.username ?: "").let { true }
            null -> null
        }
    } else {
        null
    } ?: call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
}

fun main(args: Array<String>) {
    setupDB().let { ds ->
        Flyway.configure()
            .dataSource(ds)
            .locations("db/migration")
            .load()
            .migrate()
    }

    EngineMain.main(args)
}
