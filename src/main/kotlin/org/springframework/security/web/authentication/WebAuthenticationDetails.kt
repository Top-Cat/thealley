package org.springframework.security.web.authentication

import java.io.Serializable


class WebAuthenticationDetails(
    /**
     * Indicates the TCP/IP address the authentication request was received from.
     * @return the address
     */
    val remoteAddress: String?,
    /**
     * Indicates the `HttpSession` id the authentication request was received
     * from.
     * @return the session ID
     */
    val sessionId: String?
) : Serializable {

    override fun equals(obj: Any?): Boolean {
        if (obj is WebAuthenticationDetails) {
            val other = obj
            if (remoteAddress == null && other.remoteAddress != null) {
                return false
            }
            if (remoteAddress != null && other.remoteAddress == null) {
                return false
            }
            if (remoteAddress != null) {
                if (remoteAddress != other.remoteAddress) {
                    return false
                }
            }
            if (sessionId == null && other.sessionId != null) {
                return false
            }
            if (sessionId != null && other.sessionId == null) {
                return false
            }
            if (sessionId != null) {
                if (sessionId != other.sessionId) {
                    return false
                }
            }
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        var code = 7654
        if (remoteAddress != null) {
            code = code * (remoteAddress.hashCode() % 7)
        }
        if (sessionId != null) {
            code = code * (sessionId.hashCode() % 7)
        }
        return code
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(javaClass.simpleName).append(" [")
        sb.append("RemoteIpAddress=").append(remoteAddress).append(", ")
        sb.append("SessionId=").append(sessionId).append("]")
        return sb.toString()
    }

    companion object {
        private val serialVersionUID: Long = 500L
    }
}