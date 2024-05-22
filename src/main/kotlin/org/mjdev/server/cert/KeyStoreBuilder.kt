@file:Suppress("unused")

package org.mjdev.server.cert

import io.ktor.utils.io.core.*
import java.io.File
import java.io.IOException
import java.security.KeyStore
import javax.security.auth.x500.X500Principal
import kotlin.text.toCharArray

class KeyStoreBuilder internal constructor() {
    private val certificates = mutableMapOf<String, CertificateInfo>()

    fun certificate(
        alias: String,
        subject: X500Principal,
        block: CertificateBuilder.() -> Unit
    ) {
        certificates[alias] = CertificateBuilder(subject).apply(block).build()
    }

    internal fun build(): KeyStore {
        val store = KeyStore.getInstance(KeyStore.getDefaultType())
        store.load(null, null)
        certificates.forEach { (alias, info) ->
            val (certificate, keys, password, issuerCertificate) = info
            val certChain = listOfNotNull(certificate, issuerCertificate).toTypedArray()
            store.setKeyEntry(alias, keys.private, password.toCharArray(), certChain)
        }
        return store
    }
}

@Throws(IOException::class)
fun File.recreate(): File {
    if (exists()) delete()
    createNewFile()
    return this
}

@Throws(IOException::class)
fun KeyStore.saveToFile(output: File, password: String) {
    output.parentFile?.mkdirs()
    output.outputStream().use { f ->
        store(f, password.toCharArray())
    }
}