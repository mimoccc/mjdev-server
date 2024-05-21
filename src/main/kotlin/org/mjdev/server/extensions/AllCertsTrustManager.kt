package org.mjdev.server.extensions

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@Suppress("CustomizableKDocMissingDocumentation")
class AllCertsTrustManager : X509TrustManager {
    @Suppress("TrustAllX509TrustManager")
    override fun checkServerTrusted(
        chain: Array<X509Certificate>,
        authType: String
    ) {
    }

    @Suppress("TrustAllX509TrustManager")
    override fun checkClientTrusted(
        chain: Array<X509Certificate>,
        authType: String
    ) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}