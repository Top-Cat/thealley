package uk.co.thomasc.thealley.config

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2RefreshToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant
import java.util.Date

object OauthAccessTokenTable : IdTable<String>("oauth_access_token") {
    override val id: Column<EntityID<String>> = varchar("token_id", 256).entityId()
    val token = blob("token")
    val authenticationId = varchar("authentication_id", 256)
    val userName = varchar("user_name", 256)
    val clientId = varchar("client_id", 256)
    val authentication = blob("authentication")
    val refreshToken = varchar("refresh_token", 256).nullable()
}

object OauthRefreshTokenTable : IdTable<String>("oauth_refresh_token") {
    override val id: Column<EntityID<String>> = varchar("token_id", 256).entityId()
    val token = blob("token")
    val authentication = blob("authentication")
}

object NewAccessTokenTable : IdTable<String>("oa_access_token") {
    override val id: Column<EntityID<String>> = varchar("token_id", 256).entityId()
    val type = varchar("type", 256)
    val expiration = timestamp("expiration")
    val scope = varchar("scope", 256)
    val userName = varchar("user_name", 256).nullable()
    val clientId = varchar("client_id", 256)
    val refreshToken = varchar("refresh_token", 256).nullable()
}

object NewRefreshTokenTable : IdTable<String>("oa_refresh_token") {
    override val id: Column<EntityID<String>> = varchar("token_id", 256).entityId()
    val scope = varchar("scope", 256)
    val userName = varchar("user_name", 256).nullable()
    val clientId = varchar("client_id", 256)
}

data class AccessTokenObj(val key: EntityID<String>) : Entity<String>(key) {
    companion object : EntityClass<String, AccessTokenObj>(OauthAccessTokenTable)
    val token by OauthAccessTokenTable.token
    val authenticationId by OauthAccessTokenTable.authenticationId
    val userName by OauthAccessTokenTable.userName
    val clientId by OauthAccessTokenTable.clientId
    val authentication by OauthAccessTokenTable.authentication
    val refreshToken by OauthAccessTokenTable.refreshToken

    fun asAccessToken(store: AlleyTokenStore): AccessToken? {
        val tokenIS = ObjectInputStream(ByteArrayInputStream(token.bytes))
        //val authenticationIS = ObjectInputStream(ByteArrayInputStream(authentication.bytes))

        return (tokenIS.readObject() as? DefaultOAuth2AccessToken)?.let { obj ->
            AccessToken(
                obj.value ?: "",
                obj.tokenType ?: "",
                obj.expiration?.toInstant() ?: Instant.MIN,
                Identity(userName),
                clientId,
                obj.scope?.filterNotNull()?.toSet() ?: setOf(),
                obj.refreshToken?.value?.let {
                    store.refreshToken(it)
                }
            )
        }
    }
}

data class RefreshTokenObj(val key: EntityID<String>) : Entity<String>(key) {
    companion object : EntityClass<String, RefreshTokenObj>(OauthRefreshTokenTable)
    val token by OauthRefreshTokenTable.token
    val authentication by OauthRefreshTokenTable.authentication

    fun asRefreshToken(): RefreshToken? {
        val tokenIS = ObjectInputStream(ByteArrayInputStream(token.bytes))
        val authenticationIS = ObjectInputStream(ByteArrayInputStream(authentication.bytes))

        return (tokenIS.readObject() as? DefaultOAuth2RefreshToken)?.let { obj ->
            (authenticationIS.readObject() as? OAuth2Authentication)?.let { aObj ->
                RefreshToken(
                    obj.value ?: "",
                    Instant.MAX,
                    aObj.userAuthentication?.name?.let { Identity(it) },
                    aObj.oAuth2Request.clientId ?: "",
                    setOf()
                )
            }
        }
    }
}

class AlleyTokenStore : TokenStore {
    companion object {
        val codes = mutableMapOf<String, CodeToken>()
        val keygen = DefaultAuthenticationKeyGenerator()
    }

    var updateOld = true

    private fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    override fun accessToken(token: String) =
        transaction {
            NewAccessTokenTable.select {
                NewAccessTokenTable.id eq token
            }.singleOrNull()?.let {
                AccessToken(
                    it[NewAccessTokenTable.id].value,
                    it[NewAccessTokenTable.type],
                    it[NewAccessTokenTable.expiration],
                    it[NewAccessTokenTable.userName]?.let { u -> Identity(u) },
                    it[NewAccessTokenTable.clientId],
                    it[NewAccessTokenTable.scope].split(",").toSet(),
                    it[NewAccessTokenTable.refreshToken]?.let { r -> refreshToken(r) }
                )
            }
        }

    override fun codeToken(token: String): CodeToken? {
        var code = codes[token]

        if (code != null && code.expired()) {
            codes.remove(token)

            code = null
        }

        return code
    }

    override fun consumeCodeToken(token: String): CodeToken? = codes.remove(token)

    override fun refreshToken(token: String) =
        transaction {
            NewRefreshTokenTable.select {
                NewRefreshTokenTable.id eq token
            }.singleOrNull()?.let {
                RefreshToken(
                    it[NewRefreshTokenTable.id].value,
                    Instant.MAX,
                    it[NewRefreshTokenTable.userName]?.let { u -> Identity(u) },
                    it[NewRefreshTokenTable.clientId],
                    it[NewRefreshTokenTable.scope].split(",").toSet(),
                )
            }
        }

    override fun revokeAccessToken(token: String) {
        transaction {
            OauthAccessTokenTable.deleteWhere {
                OauthAccessTokenTable.id eq md5(token)
            }
        }
    }

    override fun revokeRefreshToken(token: String) {
        transaction {
            OauthRefreshTokenTable.deleteWhere {
                OauthRefreshTokenTable.id eq md5(token)
            }
        }
    }

    override fun storeAccessToken(accessToken: AccessToken) {
        val refreshStr = accessToken.refreshToken?.refreshToken
        val tokenObj = DefaultOAuth2AccessToken(object : OAuth2AccessToken, Serializable {
            override val additionalInformation: Map<String?, Any?> = mapOf()
            override val scope: Set<String?> = accessToken.scopes
            override val refreshToken: OAuth2RefreshToken = object : OAuth2RefreshToken, Serializable {
                override val value: String? = refreshStr
            }
            override val tokenType: String = accessToken.tokenType
            override val isExpired: Boolean = accessToken.expired()
            override val expiration: Date = Date(accessToken.expireTime.toEpochMilli())
            override val expiresIn: Int = accessToken.expiresIn()
            override val value: String =  accessToken.accessToken
        })

        val auth = OAuth2Authentication(
            OAuth2Request(mapOf(), accessToken.clientId, null, false, accessToken.scopes, null, null, null, null),
            accessToken.identity?.let {
                PreAuthenticatedAuthenticationToken(it.username, "")
            }
        )

        val os = ByteArrayOutputStream()
        val writer = ObjectOutputStream(os)
        writer.writeObject(tokenObj)

        val os2 = ByteArrayOutputStream()
        val writer2 = ObjectOutputStream(os2)
        writer2.writeObject(auth)

        transaction {
            if (updateOld) {
                OauthAccessTokenTable.insert {
                    it[id] = md5(accessToken.accessToken)
                    it[token] = ExposedBlob(os.toByteArray())
                    it[authenticationId] = keygen.extractKey(auth)
                    it[authentication] = ExposedBlob(os2.toByteArray())
                    it[userName] = accessToken.identity?.username ?: ""
                    it[clientId] = accessToken.clientId
                    it[refreshToken] = accessToken.refreshToken?.refreshToken?.let { refreshTokenStr -> md5(refreshTokenStr) }
                }
            }
            NewAccessTokenTable.insert {
                it[id] = accessToken.accessToken
                it[type] = accessToken.tokenType
                it[expiration] = accessToken.expireTime
                it[scope] = accessToken.scopes.joinToString(",")
                it[userName] = accessToken.identity?.username
                it[clientId] = accessToken.clientId
                it[refreshToken] = accessToken.refreshToken?.refreshToken
            }
        }

        if (accessToken.refreshToken != null) {
            storeRefreshToken(accessToken.refreshToken!!)
        }
    }

    override fun storeCodeToken(codeToken: CodeToken) {
        codes[codeToken.codeToken] = codeToken
    }

    override fun storeRefreshToken(refreshToken: RefreshToken) {
        val tokenObj = DefaultOAuth2RefreshToken(refreshToken.refreshToken)

        val auth = OAuth2Authentication(
            OAuth2Request(mapOf(), refreshToken.clientId, null, false, refreshToken.scopes, null, null, null, null),
            refreshToken.identity?.let {
                PreAuthenticatedAuthenticationToken(it.username, "")
            }
        )

        val os = ByteArrayOutputStream()
        val writer = ObjectOutputStream(os)
        writer.writeObject(tokenObj)

        val os2 = ByteArrayOutputStream()
        val writer2 = ObjectOutputStream(os2)
        writer2.writeObject(auth)

        transaction {
            if (updateOld) {
                OauthRefreshTokenTable.insert {
                    it[id] = md5(refreshToken.refreshToken)
                    it[token] = ExposedBlob(os.toByteArray())
                    it[authentication] = ExposedBlob(os2.toByteArray())
                }
            }
            NewRefreshTokenTable.insert {
                it[id] = refreshToken.refreshToken
                it[scope] = refreshToken.scopes.joinToString(",")
                it[userName] = refreshToken.identity?.username
                it[clientId] = refreshToken.clientId
            }
        }
    }

    fun upgrade() {
        updateOld = false
        transaction {
            AccessTokenObj.wrapRows(OauthAccessTokenTable.selectAll()).mapNotNull { it.asAccessToken(this@AlleyTokenStore) }
        }.forEach {
            storeAccessToken(it)
        }
        updateOld = true
    }
}