@file:Suppress("unused")

package org.mjdev.server.dynu

data class Data(
    var user: String = "",
    var password: String = "",
    var ttl: Int = DEFAULT_TTL
) {
    val isEmpty: Boolean
        get() = password.isEmpty() || user.isEmpty()

    companion object {
        const val DEFAULT_TTL = 120
    }
}