package org.mjdev.server.data

import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import io.ktor.server.http.content.staticFiles
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.host
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Service(
    private val file: File
) {

    private val serviceFileName
        get() = "${file.name}.service"

    val serviceFile
        get() = file

    val serviceDescriptorFile
        get() = serviceFile.parentFile.resolve(serviceFileName)

    val service
        get() = ServiceFile(serviceDescriptorFile)

    val serviceType
        get() = service.serviceType

    val isNodeJs: Boolean
        get() = serviceType == ServiceType.NODE_JS

    val isKts: Boolean
        get() = serviceType == ServiceType.KTS

    val isStatic: Boolean
        get() = (!isNodeJs) && (!isKts)

    val servicePort
        get() = service.servicePort.toInt()

    val serviceHost
        get() = service.serviceHost

    val hostName: String
        get() = System.getProperties()["host"]?.toString() ?: "localhost"

    val serviceHostName: String
        get() = if (serviceHost == "*") hostName else "$serviceHost.$hostName"

    fun mapRoute(routing: Routing) {
        println("Configuring service ${service.serviceName} on port $servicePort for host: $serviceHostName")
        routing.host(
            serviceHostName,
            servicePort
        ) {
            when {
                isStatic -> routing.staticFiles("/", file)
                isNodeJs -> routing.nodeJsApp(this@Service)
                isKts -> routing.ktsApp(this@Service)
                else -> routing.staticFiles("/", file)
            }
        }
    }
}

// todo: implement ktsApp
@OptIn(KtorExperimentalLocationsAPI::class)
private fun Routing.ktsApp(service: Service): Route {
    println(
        "Service ${
            service.serviceHostName
        } not started, unimplemented type: ${
            service.serviceType
        }"
    )
    return get<String> {
        "Service ${
            service.serviceHostName
        } not started, unimplemented type: ${
            service.serviceType
        }"
    }
}

// todo: implement nodeJsApp
@OptIn(KtorExperimentalLocationsAPI::class)
private fun Routing.nodeJsApp(service: Service): Route {
    println(
        "Service ${
            service.serviceHostName
        } not started, unimplemented type: ${
            service.serviceType
        }"
    )
    return get<String> {
        "Service ${
            service.serviceHostName
        } not started, unimplemented type: ${
            service.serviceType
        }"
    }
}
