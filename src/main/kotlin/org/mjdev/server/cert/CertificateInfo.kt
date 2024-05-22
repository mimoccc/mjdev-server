package org.mjdev.server.cert

import java.security.*
import java.security.cert.Certificate

data class CertificateInfo(
    val certificate: Certificate,
    val keys: KeyPair,
    val password: String,
    val issuerCertificate: Certificate?,
)