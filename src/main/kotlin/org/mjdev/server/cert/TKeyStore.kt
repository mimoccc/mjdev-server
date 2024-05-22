package org.mjdev.server.cert

import java.io.File
import java.security.KeyStore
import javax.security.auth.x500.X500Principal

abstract class TKeyStore(
    val keyAlias: String = "mjdev",
    val keyPassword: String = "1234567890",
    val keyStoreFile: File = File("keystore.jks"),
    val domainsList: List<String> = listOf("127.0.0.1", "0.0.0.0", "localhost"),
    private val commonName: String = "localhost",
    private val organizationUnit: String = "it-development",
    private val organization: String = "mjdev",
    private val country: String = "CZ"
) {
    val subject: X500Principal
        get() = X500Principal(
            "CN=$commonName, OU=$organizationUnit, O=$organization, C=$country"
        )

    abstract fun build(): KeyStore

    companion object {
        fun buildKeyStore(block: KeyStoreBuilder.() -> Unit): KeyStore =
            KeyStoreBuilder().apply(block).build()
    }
}
