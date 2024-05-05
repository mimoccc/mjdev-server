package org.mjdev.server.data

enum class ServiceType(
    val type:String
) {
    NODE_JS("nodejs"),
    KTS("kts"),
    STATIC("static");

    companion object {
        operator fun invoke(value: String) = entries.firstOrNull { it.type == value } ?: STATIC
    }
}