package org.mjdev.server.server

import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.locations.Locations
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.httpsredirect.HttpsRedirect
import io.ktor.server.routing.*
import java.security.KeyStore
import io.ktor.client.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.request.port
import io.ktor.server.request.uri
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import org.slf4j.event.Level
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import io.ktor.util.*
import org.mjdev.gradle.data.ServiceFile
import org.mjdev.server.plugins.HttpDataResponse
import org.mjdev.server.extensions.AllCertsTrustManager
import java.io.File
import org.mjdev.server.extensions.ThrowableExt.toErrorString

@Suppress(
    "MemberVisibilityCanBePrivate", "unused", "OPT_IN_USAGE",
    "CustomizableKDocMissingDocumentation"
)
class Server {
    private val isDebug: Boolean = true
    private val httpPort: Int = if (isDebug) 8080 else 80
    private val httpsPort: Int = if (isDebug) 8443 else 443
    private val root: String = "services"

    // todo all hosts
    private val hostNameFile = File("/etc/hostname")
    private val hostName = hostNameFile.readText().trim()

    // todo
    private val rootFile = File(root)
    private val keyStoreFileName: String = "keystore.jks"
    private val keyStoreFile = File(keyStoreFileName)
    private var engine: NettyApplicationEngine? = null

    private val environment: ApplicationEngineEnvironment
    private val logLevel = Level.TRACE
    private val onStarted: (application: Application) -> Unit = {}
    private val onStopped: (application: Application) -> Unit = {}
    private val onCreateHeaders: () -> Map<String, String>? = { null }
    private val onException: (t: Throwable) -> HttpDataResponse? = { null }
    private val onStatus: (status: HttpStatusCode) -> HttpDataResponse? = { null }

    private val services = parseServices(rootFile)
    private var keyStore = createCertificate(services)

    init {
        val module: Application.() -> Unit = {
//            install(AppStatePlugin) {
//                onServerStart = { app -> onStarted(app) }
//                onServerStop = { app -> onStopped(app) }
//            }
//            install(SimplePlugin)
            install(Routing)
            install(DefaultHeaders)
            install(Locations)
            // todo client error
//            install(Compression) {
//                gzip()
//                deflate()
//            }
            install(ForwardedHeaders)
            install(HttpsRedirect) {
                sslPort = httpsPort
                permanentRedirect = true
            }
            install(CallLogging) {
                level = logLevel
//                logger = AppLogger
            }
//            install(StatusPages) {
//                status(HttpStatusCode.NotFound) { call, status ->
//                    (onStatus(status) ?: HttpDataResponse(status)).let { r ->
//                        call.respondBytes(
//                            provider = r.dataProvider,
//                            status = r.code
//                        )
//                    }
//                }
//                exception<Throwable> { call, cause ->
//                    (onException(cause) ?: HttpDataResponse(cause)).let { r ->
//                        call.respondBytes(
//                            provider = r.dataProvider,
//                            status = r.code
//                        )
//                    }
//                }
//            }
//            install(ShutDownUrl.ApplicationCallPlugin) {
//                shutDownUrl = "/shutdown"
//                exitCodeSupplier = { 0 }
//            }
            routing {
                services.forEach { service ->
                    mapRoute(
                        service,
                        isDebug = isDebug
                    )
                }
            }
        }
        environment = applicationEngineEnvironment {
            connector {
                port = httpPort
            }
            sslConnector(
                keyStore = keyStore,
                keyAlias = hostName,
                keyStorePassword = { hostName.toCharArray() },
                privateKeyPassword = { hostName.toCharArray() }) {
                port = httpsPort
                keyStorePath = keyStoreFile
            }
            module(module)
        }
        println("Got ${services.size} services from: $rootFile.")
        engine = embeddedServer(Netty, environment)
    }

    private fun createCertificate(services: List<ServiceFile>): KeyStore {
        val keyFile = rootFile.resolve(keyStoreFileName)
        val keyStore = buildKeyStore {
            certificate(hostName) {
                password = hostName
                domains = services.map { service ->
                    service.serviceHostName
                }.toMutableList().apply {
                    add(hostName)
                }
            }
        }
        keyStore.saveToFile(keyFile, hostName)
        return keyStore
    }

    private fun parseServices(
        services: File
    ) = runCatching {
        services.listFiles()?.toList()?.mapNotNull { file ->
            if (file.name.endsWith(".service")) ServiceFile(file) else null
        }?.filter { f -> f.isEnabled } ?: emptyList()
    }.onFailure { error ->
        println("Error parsing services: $error")
    }.getOrDefault(emptyList())

    fun start() {
        engine?.start(wait = true)
    }

    fun stop() {
        engine?.stop()
    }

    fun Routing.mapRoute(
        service: ServiceFile,
        isDebug: Boolean = false,
        hosts: List<Pair<String, Int>> = service.hosts.let { list ->
            if (isDebug) {
                list.toMutableList().apply { removeIf { entry -> entry.second < 1000 } }
            } else {
                list
            }
        },
    ): Route = host(
        hosts.map { it.first },
        hosts.map { it.second }
    ) {
        println("# Service: ${service.serviceName}")
        hosts.forEach { host ->
            val type = if (host.second.toString().endsWith("443")) "https" else "http"
            println("- Mapping route -> $type://${host.first}:${host.second}")
        }
        when {
            service.isStatic -> {
                println("- Service ${service.serviceHostName} is static.")
                staticFiles("/", service.serviceDir)
            }

            service.isProxy -> {
                println("- Service ${service.serviceHostName} is proxy.")
                proxySelf(service, isDebug)
            }

            service.isNodeJs -> {
                println("- Service ${service.serviceHostName} is node js app.")
                nodeJsApp(service, isDebug)
            }

            service.isKts -> {
                println("- Service ${service.serviceHostName} is kts app.")
                ktsApp(service, isDebug)
            }

            service.isCustom -> {
                println("- Service ${service.serviceHostName} is custom app.")
                customApp(service, isDebug)
            }

            else -> {
                println("- Service ${service.serviceHostName} is unknown app.")
                println("- Routing to default.")
                staticFiles("/", service.serviceDir)
            }
        }
    }

    private fun client() = HttpClient(OkHttp) {
        install(Logging)
        install(ContentNegotiation) {
            clearIgnoredTypes()
        }
//        install(ContentEncoding) {
//            gzip()
//            deflate()
//        }
        expectSuccess = false
        developmentMode = isDebug
        engine {
            config {
                val trustAllCert = AllCertsTrustManager()
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, arrayOf(trustAllCert), SecureRandom())
                sslSocketFactory(sslContext.socketFactory, trustAllCert)
            }
        }
    }

    private fun Route.proxySelf(
        service: ServiceFile,
        isDebug: Boolean = false
    ): Route = route("{...}") {
        handle {
            val request = call.request
            val isSecure = request.port().toString().endsWith("443")
            val query = request.queryParameters.toMap()
                .map { (k, v) -> "$k=$v" }
                .joinToString("&")
            val port: Int = when (service.servicePort) {
                "*" -> if (isDebug) if (isSecure) 8443 else 8080 else if (isSecure) 443 else 80
                else -> service.servicePort.toInt()
            }
            val document: String = request.uri
            val reqURL = "${
                if (service.isSSL) "https" else "http"
            }://" + "${hostName}:${port}${
                if (document != "" && document != "/") document else "/"
            }${
                if (query.isNotEmpty()) "?$query" else ""
            }".replace("//", "/")
            println("Requesting url : $reqURL")
            runCatching {
                client().get(reqURL) {
                    headers.appendAll(request.headers)
                }
            }.onFailure { error ->
                if (isDebug) {
                    call.respondText {
                        error.toErrorString("Request url" to reqURL)
                    }
                } else {
                    call.respondRedirect {
                        "https://${hostName}:${httpsPort}/500.html"
                    }
                }
            }.onSuccess { response ->
                call.respondBytes(
                    contentType = response.contentType(),
                    status = response.status,
                    provider = { response.readBytes() }
                )
            }
        }
    }

//    fun Route.redirect(
//        redirectUrl: String,
//        port: Int
//    ): Route = get {
//        call.respondRedirect("http://$redirectUrl:$port")
//    }

//    fun Route.redirect(
//        redirectUrl: String,
//    ): Route = get {
//        call.respondRedirect("http://$redirectUrl")
//    }

    private fun Route.customApp(
        service: ServiceFile,
        isDebug: Boolean = false
    ): Route {
        return proxySelf(service, isDebug)
    }

    private fun Route.ktsApp(
        service: ServiceFile,
        isDebug: Boolean = false
    ): Route {
        return proxySelf(service, isDebug)
    }

    private fun Route.nodeJsApp(
        service: ServiceFile,
        isDebug: Boolean = false
    ): Route {
        return proxySelf(service, isDebug)
    }

//    val routeKey = AttributeKey<Route>("RequestRoute")
//
//    @Suppress("PropertyName")
//    val RequestRoutePlugin = createApplicationPlugin("RequestRoutePlugin") {
//        on(MonitoringEvent(Routing.RoutingCallStarted)) { call ->
//            call.attributes.put(routeKey, call.route)
//        }
//    }
//
//    fun ApplicationCall.requestRoute() =
//        attributes[routeKey]

}
