package org.mjdev.server

import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.httpsredirect.HttpsRedirect
import io.ktor.server.routing.*
import org.mjdev.server.data.Service
import java.io.*
import java.security.KeyStore

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate", "unused")
class Server(
    val httpPort: Int = 80,
    val sslPort: Int = 443,
    val root: String = "services",
    val host: String = "mjdev.org",
    val keyStoreFileName: String = "keystore.jks",
) {
    private val rootFile = File(root)
    private val keyStoreFile = File(keyStoreFileName)
    private var engine: NettyApplicationEngine? = null
    private var services: List<Service>
    private var keyStore: KeyStore
    private val environment: ApplicationEngineEnvironment

    init {
        services = parseServices(rootFile)
        keyStore = createCertificate(services)
        val module: Application.() -> Unit = {
            install(Routing)
            install(DefaultHeaders)
            install(CallLogging)
            install(Compression) {
                gzip()
                deflate()
            }
            install(ForwardedHeaders)
            install(HttpsRedirect) {
                sslPort = this@Server.sslPort
                permanentRedirect = true
            }
            install(ShutDownUrl.ApplicationCallPlugin) {
                shutDownUrl = "/shutdown"
                exitCodeSupplier = { 0 }
            }
            routing {
                services.forEach { service ->
                    service.mapRoute(this)
                }
            }
        }
        environment = applicationEngineEnvironment {
            connector {
                port = httpPort
            }
            sslConnector(
                keyStore = keyStore,
                keyAlias = host,
                keyStorePassword = { host.toCharArray() },
                privateKeyPassword = { host.toCharArray() }) {
                port = sslPort
                keyStorePath = keyStoreFile
            }
            module(module)
        }
        println("Got ${services.size} services from: $rootFile.")
        engine = embeddedServer(Netty, environment)
    }

    private fun createCertificate(services: List<Service>): KeyStore {
        val keyFile = rootFile.resolve(keyStoreFileName)
        val keyStore = buildKeyStore {
            certificate(host) {
                password = host
                domains = services.map { it.serviceHostName }
            }
        }
        keyStore.saveToFile(keyFile, host)
        return keyStore
    }

    private fun parseServices(services: File) =
        services.listFiles()?.mapNotNull { file ->
            when {
                file.isDirectory -> Service(file)
                else -> null
            }
        } ?: emptyList()

    fun start() {
        engine?.start(wait = true)
    }

    fun stop() {
        engine?.stop()
    }
}
