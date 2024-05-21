@file:Suppress("CustomizableKDocMissingDocumentation", "PackageDirectoryMismatch")

package org.mjdev.gradle.data

import java.io.File
import java.util.Locale

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ServiceFile(
    val serviceFile: File
) {

    private val hostNameFile
        get() = File("/etc/hostname")

    val hostName
        get() = hostNameFile.readText().trim()

    private val props = serviceFile.readLines().associate {
        val (key, value) = it.split("=")
        key to value
    }

    private val serviceGitUrl
        get() = props["git"] ?: ""

    val serviceName
        get() = props["name"] ?: serviceFile.name.split(".").first()

    private val serviceTypeName
        get() = props["type"] ?: "static"

    private val serviceEnabled
        get() = props["enabled"] ?: "false"

    private val serviceHost
        get() = props["host"] ?: "*"

    val servicePort
        get() = props["port"] ?: "*"

    private val serviceKeepSrc
        get() = props["keep"] ?: "true"

    private val serviceIsSSL
        get() = props["isSSL"] ?: "false"

    private val serviceDependencies
        get() = props["dependencies"] ?: ""

    private val serviceConfig
        get() = props["config"] ?: ""

    private val serviceCustomScript
        get() = props["customScript"] ?: ""

    private val serviceStart
        get() = props["start"] ?: ""

    private val serviceStop
        get() = props["stop"] ?: ""

    val serviceKeepSrcFiles
        get() = parseBoolean(serviceKeepSrc) ?: true

    val serviceDir
        get() = serviceFile.parentFile.resolve(serviceName)

    val isGit
        get() = serviceGitUrl.isNotEmpty()

    val isStatic
        get() = (serviceType == ServiceType.STATIC)

    val isNodeJs
        get() = serviceType == ServiceType.NODE_JS

    val isKts
        get() = serviceType == ServiceType.KTS

    val isCustom
        get() = serviceType == ServiceType.CUSTOM

    val isAndroid
        get() = serviceType == ServiceType.ANDROID

    val isProxy
        get() = serviceType == ServiceType.PROXY

    val isSSL
        get() = parseBoolean(serviceIsSSL) ?: false

    val serviceDirExists
        get() = serviceDir.exists() && (serviceDir.listFiles()?.isNotEmpty() ?: false)

    val isInstalled
        get() = isProxy || serviceDirExists

    val gitUrl
        get() = serviceGitUrl

    val serviceType
        get() = ServiceType(serviceTypeName)

    val isNotEmpty
        get() = serviceFile.listFiles()?.isNotEmpty() ?: false

    val exists
        get() = serviceFile.isDirectory && serviceFile.exists() && isNotEmpty

    val isEnabled
        get() = parseBoolean(serviceEnabled) ?: false

    val isDisabled
        get() = !isEnabled

    val serviceHostName
        get() = "$serviceName.$hostName"

    val ports = listOf(80, 8080, 443, 8443)

    val hosts = serviceHost.let { h ->
        val result = mutableListOf<Pair<String, Int>>()
        val names: List<String> = when {
            h == "*" -> listOf("localhost", "127.0.0.1", "0.0.0.0", hostName)
            else -> h.split(",").map { it.trim() }
        }.toMutableList().apply { add(serviceHostName) }
        names.forEach { host ->
            ports.forEach { port ->
                result.add(host to port)
            }
        }
        result
    }

    private fun parseBoolean(value: Any): Boolean? = when (value) {
        is Boolean -> value
        is CharSequence ->
            when (value.toString().lowercase(Locale.US)) {
                "true" -> true
                "false" -> false
                else -> null
            }

        is Number ->
            when (value.toInt()) {
                0 -> false
                1 -> true
                else -> null
            }

        else -> null
    }

    fun resolve(path: String): File = serviceDir.resolve(path)

}