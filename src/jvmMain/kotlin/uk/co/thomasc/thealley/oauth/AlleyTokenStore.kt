package uk.co.thomasc.thealley.oauth

import nl.myndocs.oauth2.client.inmemory.InMemoryClient
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class AlleyTokenStore(private val clientStore: InMemoryClient) : TokenStore {
    companion object {
        val codes = mutableMapOf<String, CodeToken>()
    }

    override fun accessToken(token: String) =
        transaction {
            AccessTokenTable.selectAll().where {
                AccessTokenTable.id eq token
            }.singleOrNull()?.let {
                AccessToken(
                    it[AccessTokenTable.id].value,
                    it[AccessTokenTable.type],
                    it[AccessTokenTable.expiration],
                    it[AccessTokenTable.userName]?.let { u -> Identity(u) },
                    it[AccessTokenTable.clientId],
                    it[AccessTokenTable.scope].split(",").toSet(),
                    it[AccessTokenTable.refreshToken]?.let { r -> refreshToken(r) }
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
            RefreshTokenTable.selectAll().where {
                RefreshTokenTable.id eq token
            }.singleOrNull()?.let {
                RefreshToken(
                    it[RefreshTokenTable.id].value,
                    Instant.MAX,
                    it[RefreshTokenTable.userName]?.let { u -> Identity(u) },
                    it[RefreshTokenTable.clientId],
                    it[RefreshTokenTable.scope].split(",").toSet()
                )
            }
        }

    override fun revokeAccessToken(token: String) {
        transaction {
            AccessTokenTable.deleteWhere {
                id eq token
            }
        }
    }

    override fun revokeRefreshToken(token: String) {
        transaction {
            RefreshTokenTable.deleteWhere {
                id eq token
            }
        }
    }

    override fun storeAccessToken(accessToken: AccessToken) {
        transaction {
            AccessTokenTable.insert {
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
            RefreshTokenTable.insert {
                it[id] = refreshToken.refreshToken
                it[scope] = refreshToken.scopes.joinToString(",")
                it[userName] = refreshToken.identity?.username
                it[clientId] = refreshToken.clientId
            }
        }
    }

    override fun tokenInfo(token: String) = (accessToken(token) ?: throw InvalidGrantException()).let { at ->
        TokenInfo(
            at.identity,
            clientStore.clientOf(at.clientId) ?: throw InvalidClientException(),
            at.scopes
        )
    }
}
