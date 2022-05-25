package uk.co.thomasc.thealley.config

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant

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

class AlleyTokenStore : TokenStore {
    companion object {
        val codes = mutableMapOf<String, CodeToken>()
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
            NewAccessTokenTable.deleteWhere {
                NewAccessTokenTable.id eq token
            }
        }
    }

    override fun revokeRefreshToken(token: String) {
        transaction {
            NewRefreshTokenTable.deleteWhere {
                NewRefreshTokenTable.id eq token
            }
        }
    }

    override fun storeAccessToken(accessToken: AccessToken) {
        transaction {
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
        transaction {
            NewRefreshTokenTable.insert {
                it[id] = refreshToken.refreshToken
                it[scope] = refreshToken.scopes.joinToString(",")
                it[userName] = refreshToken.identity?.username
                it[clientId] = refreshToken.clientId
            }
        }
    }
}