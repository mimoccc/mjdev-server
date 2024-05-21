@file:Suppress("CustomizableKDocMissingDocumentation", "PackageDirectoryMismatch")
package org.mjdev.gradle.data

enum class ServiceType(
    val type: String
) {
    STATIC("static"),
    PROXY("proxy"),
    CUSTOM("custom"),
    NODE_JS("node"),
    KTS("kts"),
    ANDROID("android");

    companion object {
        operator fun invoke(value: String) = values().firstOrNull { it.type == value } ?: STATIC
    }
}