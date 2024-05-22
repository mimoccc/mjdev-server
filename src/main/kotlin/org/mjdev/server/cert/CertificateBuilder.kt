@file:Suppress("unused")

package org.mjdev.server.cert

import io.ktor.network.tls.*
import io.ktor.network.tls.certificates.*
import io.ktor.network.tls.extensions.*
import io.ktor.utils.io.core.*
import java.math.BigInteger
import java.net.Inet4Address
import java.net.InetAddress
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.security.auth.x500.X500Principal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

class CertificateBuilder internal constructor(
    private val subject: X500Principal
) {
    private var hash: HashAlgorithm = HashAlgorithm.SHA1
    private var sign: SignatureAlgorithm = SignatureAlgorithm.RSA

    lateinit var password: String

    private var daysValid: Long = 3

    private var keySizeInBits: Int = 1024

    private var keyType: KeyType = KeyType.Server

    var domains: List<String> = listOf("localhost")

    private var ipAddresses: List<InetAddress> = listOf(Inet4Address.getByName("127.0.0.1"))

    private var issuer: CertificateIssuer? = null

    private data class CertificateIssuer(
        val name: X500Principal,
        val keyPair: KeyPair,
        val keyCertificate: Certificate,
    )

    fun signWith(
        issuerKeyPair: KeyPair,
        issuerKeyCertificate: X509Certificate,
    ) {
        issuer = CertificateIssuer(
            name = issuerKeyCertificate.subjectX500Principal,
            keyPair = issuerKeyPair,
            keyCertificate = issuerKeyCertificate,
        )
    }

    fun signWith(
        issuerKeyPair: KeyPair,
        issuerKeyCertificate: Certificate,
        issuerName: X500Principal,
    ) {
        issuer = CertificateIssuer(
            name = issuerName,
            keyPair = issuerKeyPair,
            keyCertificate = issuerKeyCertificate
        )
    }

    fun build(): CertificateInfo {
        val algorithm = HashAndSign(hash, sign)
        val keys = KeyPairGenerator.getInstance(
            keysGenerationAlgorithm(
                algorithm.name
            )
        ).apply {
            initialize(keySizeInBits)
        }.genKeyPair()!!
        val cert = generateX509Certificate(
            issuer = issuer?.name ?: subject,
            subject = subject,
            publicKey = keys.public,
            signerKeyPair = issuer?.keyPair ?: keys,
            algorithm = algorithm.name,
            validityDuration = daysValid.days,
            keyType = keyType,
            domains = domains,
            ipAddresses = ipAddresses,
        )
        return CertificateInfo(cert, keys, password, issuer?.keyCertificate)
    }
}

fun generateX509Certificate(
    subject: X500Principal,
    issuer: X500Principal,
    publicKey: PublicKey,
    signerKeyPair: KeyPair,
    algorithm: String,
    validityDuration: Duration = 3.days,
    keyType: KeyType = KeyType.Server,
    domains: List<String> = listOf("127.0.0.1", "localhost"),
    ipAddresses: List<InetAddress> = listOf(Inet4Address.getByName("127.0.0.1")),
): X509Certificate {
    val now = Instant.now()
    val certificateBytes = buildPacket {
        writeCertificate(
            issuer = issuer,
            subject = subject,
            publicKey = publicKey,
            signerKeyPair = signerKeyPair,
            algorithm = algorithm,
            validFrom = now,
            validUntil = now.plus(validityDuration.toJavaDuration()),
            domains = domains,
            ipAddresses = ipAddresses,
            keyType = keyType
        )
    }.readBytes()
    val cert = CertificateFactory
        .getInstance("X.509")
        .generateCertificate(certificateBytes.inputStream())
    cert.verify(signerKeyPair.public)
    return cert as X509Certificate
}

fun Int.derLength(): Int {
    require(this >= 0)
    if (this == 0) return 0
    var mask = 0x7f
    var byteCount = 1
    while (true) {
        if (this and mask == this) break
        mask = mask or (mask shl 7)
        byteCount++
    }
    return byteCount
}

fun BytePacketBuilder.writeDerInt(value: Int) {
    require(value >= 0)
    val byteCount = value.derLength()
    repeat(byteCount) { idx ->
        val part = (value shr ((byteCount - idx - 1) * 7) and 0x7f)
        if (idx == byteCount - 1) {
            writeByte(part.toByte())
        } else {
            writeByte((part or 0x80).toByte())
        }
    }
}

fun BytePacketBuilder.writeDerType(kind: Int, typeIdentifier: Int, simpleType: Boolean) {
    require(kind in 0..3)
    require(typeIdentifier >= 0)
    if (typeIdentifier in 0..30) {
        val singleByte = (kind shl 6) or typeIdentifier or (if (simpleType) 0 else 0x20)
        val byteValue = singleByte.toByte()
        writeByte(byteValue)
    } else {
        val firstByte = (kind shl 6) or 0x1f or (if (simpleType) 0 else 0x20)
        writeByte(firstByte.toByte())
        writeDerInt(typeIdentifier)
    }
}

fun BytePacketBuilder.writeDerLength(length: Int) {
    require(length >= 0)
    when {
        length <= 0x7f -> writeByte(length.toByte())
        length <= 0xff -> {
            writeByte(0x81.toByte())
            writeByte(length.toByte())
        }
        length <= 0xffff -> {
            writeByte(0x82.toByte())
            writeByte((length ushr 8).toByte())
            writeByte(length.toByte())
        }
        length <= 0xffffff -> {
            writeByte(0x83.toByte())
            writeByte((length ushr 16).toByte())
            writeByte(((length ushr 8) and 0xff).toByte())
            writeByte(length.toByte())
        }
        else -> {
            writeByte(0x84.toByte())
            writeByte((length ushr 24).toByte())
            writeByte(((length ushr 16) and 0xff).toByte())
            writeByte(((length ushr 8) and 0xff).toByte())
            writeByte(length.toByte())
        }
    }
}

fun BytePacketBuilder.writeDerSequence(block: BytePacketBuilder.() -> Unit) {
    val sub = buildPacket { block() }
    writeDerType(0, 0x10, false)
    writeDerLength(sub.remaining.toInt())
    writePacket(sub)
}

fun BytePacketBuilder.writeDerObjectIdentifier(identifier: IntArray) {
    require(identifier.size >= 2)
    require(identifier[0] in 0..2)
    require(identifier[0] == 2 || identifier[1] in 0..39)
    val sub = buildPacket {
        writeDerInt(identifier[0] * 40 + identifier[1])
        for (i in 2..identifier.lastIndex) {
            writeDerInt(identifier[i])
        }
    }
    writeDerType(0, 6, true)
    writeDerLength(sub.remaining.toInt())
    writePacket(sub)
}

fun BytePacketBuilder.writeDerObjectIdentifier(identifier: OID) {
    writeDerObjectIdentifier(identifier.asArray)
}

fun BytePacketBuilder.writeDerNull() {
    writeShort(0x0500)
}

fun BytePacketBuilder.writeDerBitString(array: ByteArray, unused: Int = 0) {
    require(unused in 0..7)
    writeDerType(0, 3, true)
    writeDerLength(array.size + 1)
    writeByte(unused.toByte())
    writeFully(array)
}

fun BytePacketBuilder.writeCertificate(
    issuer: X500Principal,
    subject: X500Principal,
    publicKey: PublicKey,
    algorithm: String,
    validFrom: Instant,
    validUntil: Instant,
    domains: List<String>,
    ipAddresses: List<InetAddress>,
    signerKeyPair: KeyPair,
    keyType: KeyType = KeyType.Server,
) {
    require(validFrom < validUntil) { "validFrom must be before validUntil" }
    val certInfo = buildPacket {
        writeX509Info(
            algorithm,
            issuer,
            subject,
            publicKey,
            validFrom,
            validUntil,
            domains,
            ipAddresses,
            keyType
        )
    }
    val certInfoBytes = certInfo.readBytes()
    val signature = Signature.getInstance(algorithm)
    signature.initSign(signerKeyPair.private)
    signature.update(certInfoBytes)
    val signed = signature.sign()
    writeDerSequence {
        writeFully(certInfoBytes)
        writeDerSequence {
            writeDerObjectIdentifier(OID.fromAlgorithm(algorithm))
            writeDerNull()
        }
        writeDerBitString(signed)
    }
}

fun BytePacketBuilder.writeAsnInt(value: Int) {
    writeDerType(0, 2, true)
    val encoded = buildPacket {
        var skip = true
        for (idx in 0..3) {
            val part = (value ushr ((4 - idx - 1) * 8) and 0xff)
            if (part == 0 && skip) {
                continue
            } else {
                skip = false
            }
            writeByte(part.toByte())
        }
    }
    writeDerLength(encoded.remaining.toInt())
    writePacket(encoded)
}

fun BytePacketBuilder.writeVersion(v: Int = 2) {
    writeDerType(2, 0, false)
    val encoded = buildPacket {
        writeAsnInt(v)
    }
    writeDerLength(encoded.remaining.toInt())
    writePacket(encoded)
}

fun BytePacketBuilder.writeAsnInt(value: BigInteger) {
    writeDerType(0, 2, true)
    val encoded = value.toByteArray()
    writeDerLength(encoded.size)
    writeFully(encoded)
}

fun BytePacketBuilder.writeAlgorithmIdentifier(algorithm: String) {
    writeDerSequence {
        val oid = OID.fromAlgorithm(algorithm)
        writeDerObjectIdentifier(oid)
        writeDerNull()
    }
}

fun BytePacketBuilder.writeX500Principal(dName: X500Principal) {
    writeFully(dName.encoded)
}

fun BytePacketBuilder.writeDerUTF8String(s: String, type: Int = 0x0c) {
    val sub = buildPacket {
        writeText(s)
    }
    writeDerType(0, type, true)
    writeDerLength(sub.remaining.toInt())
    writePacket(sub)
}

fun BytePacketBuilder.writeDerUTCTime(date: Instant) {
    writeDerUTF8String(
        DateTimeFormatter.ofPattern("yyMMddHHmmss'Z'").format(date.atZone(ZoneOffset.UTC)),
        0x17
    )
}

fun BytePacketBuilder.writeDerGeneralizedTime(date: Instant) {
    writeDerUTF8String(
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'").format(date.atZone(ZoneOffset.UTC)),
        0x18
    )
}

fun Boolean.toUByte(): UByte = if (this) {
    255.toUByte()
} else {
    0.toUByte()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun BytePacketBuilder.writeDerBoolean(value: Boolean) {
    writeDerType(0, 1, true)
    writeDerLength(1)
    writeUByte(value.toUByte())
}

fun BytePacketBuilder.writeDerOctetString(block: BytePacketBuilder.() -> Unit) {
    val sub = buildPacket { block() }
    writeDerType(0, 4, true)
    writeDerLength(sub.remaining.toInt())
    writePacket(sub)
}

fun BytePacketBuilder.caExtension() {
    writeDerSequence {
        writeDerObjectIdentifier(OID.BasicConstraints)
        writeDerBoolean(true)
        writeDerOctetString {
            writeDerSequence {
                writeDerBoolean(true)
            }
        }
    }
}

fun BytePacketBuilder.extKeyUsage(content: BytePacketBuilder.() -> Unit) {
    writeDerSequence {
        writeDerObjectIdentifier(OID.ExtKeyUsage)
        writeDerOctetString {
            content()
        }
    }
}

fun BytePacketBuilder.writeX509Extension(id: Int, builder: BytePacketBuilder.() -> Unit) {
    writeByte((0x80 or id).toByte())
    val packet = buildPacket { builder() }
    writeDerLength(packet.remaining.toInt())
    writePacket(packet)
}

fun BytePacketBuilder.subjectAlternativeNames(
    domains: List<String>,
    ipAddresses: List<InetAddress>
) {
    writeDerSequence {
        writeDerObjectIdentifier(OID.SubjectAltName)
        writeDerOctetString {
            writeDerSequence {
                for (domain in domains) {
                    writeX509Extension(2) {
                        // DNSName
                        writeFully(domain.toByteArray())
                    }
                }
                for (ip in ipAddresses) {
                    writeX509Extension(7) {
                        // IP address
                        writeFully(ip.address)
                    }
                }
            }
        }
    }
}

fun BytePacketBuilder.serverAuth() {
    writeDerSequence {
        writeDerObjectIdentifier(OID.ServerAuth)
    }
}

fun BytePacketBuilder.clientAuth() {
    writeDerSequence {
        writeDerObjectIdentifier(OID.ClientAuth)
    }
}

fun BytePacketBuilder.writeX509Info(
    algorithm: String,
    issuer: X500Principal,
    subject: X500Principal,
    publicKey: PublicKey,
    validFrom: Instant,
    validUntil: Instant,
    domains: List<String>,
    ipAddresses: List<InetAddress>,
    keyType: KeyType = KeyType.Server
) {
    val version = BigInteger(64, SecureRandom())
    writeDerSequence {
        writeVersion(2) // v3
        writeAsnInt(version) // certificate version
        writeAlgorithmIdentifier(algorithm)
        writeX500Principal(issuer)
        writeDerSequence {
            writeDerUTCTime(validFrom)
            writeDerGeneralizedTime(validUntil)
        }
        writeX500Principal(subject)
        writeFully(publicKey.encoded)
        writeByte(0xa3.toByte())
        val extensions = buildPacket {
            writeDerSequence {
                when (keyType) {
                    KeyType.CA -> {
                        caExtension()
                    }
                    KeyType.Server -> {
                        extKeyUsage { serverAuth() }
                        subjectAlternativeNames(domains, ipAddresses)
                    }
                    KeyType.Client -> {
                        extKeyUsage { clientAuth() }
                    }
                }
            }
        }
        writeDerLength(extensions.remaining.toInt())
        writePacket(extensions)
    }
}
