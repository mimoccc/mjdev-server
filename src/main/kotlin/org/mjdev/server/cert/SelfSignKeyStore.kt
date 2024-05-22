@file:Suppress("unused")

package org.mjdev.server.cert

import java.io.File
import java.security.KeyStore

class SelfSignKeyStore(
    keyAlias: String = "mjdev",
    keyPassword: String = "1234567890",
    keyStoreFile: File = File("keystore.jks"),
    domainsList: List<String> = listOf("127.0.0.1", "0.0.0.0", "localhost"),
    commonName: String = "localhost",
    organizationUnit: String = "it-development",
    organization: String = "mjdev",
    country: String = "CZ"
) : TKeyStore(
    keyAlias,
    keyPassword,
    keyStoreFile,
    domainsList,
    commonName,
    organizationUnit,
    organization,
    country
) {
    override fun build(): KeyStore = buildKeyStore {
        certificate(keyAlias, subject) {
            password = keyPassword
            domains = domainsList
        }
    }.apply {
        saveToFile(keyStoreFile, keyPassword)
    }
}