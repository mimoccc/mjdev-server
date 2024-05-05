package org.mjdev.server.data

import java.io.File
import java.util.Locale

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ServiceFile(
    val file: File
) {

    private val props = file.readLines().associate {
        val (key, value) = it.split("=")
        key to value
    }

    private val serviceGitUrl: String
        get() = props["git"] ?: ""

    val serviceName: String
        get() = props["name"] ?: file.name.split(".").first()

    private val serviceTypeName
        get() = props["type"] ?: "static"

    private val serviceEnabled
        get() = props["enabled"] ?: "false"

    val serviceHost
        get() = props["host"] ?: "*"

    val servicePort
        get() = props["port"] ?: "0"

    val serviceDir
        get() = file.parentFile.resolve(serviceName)

    val isGit
        get()= serviceGitUrl.isNotEmpty()

    val gitUrl
        get() = serviceGitUrl

    val serviceType: ServiceType
        get() = ServiceType(serviceTypeName)

    val isNotEmpty: Boolean
        get() = file.listFiles()?.isNotEmpty() ?: false

    val exists: Boolean
        get() = file.isDirectory && file.exists() && isNotEmpty

    val isEnabled: Boolean
        get() = parseBoolean(serviceEnabled) ?: false

    val isDisabled: Boolean
        get() = !isEnabled

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

}