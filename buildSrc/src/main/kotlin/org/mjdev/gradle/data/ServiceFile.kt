package org.mjdev.gradle.data

import java.io.File
import org.mjdev.gradle.extensions.OtherExt.parseBoolean

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

    private val serviceHost
        get() = props["host"] ?: "*"

    private val servicePort
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

}