package org.mjdev.gradle.plugin.config

open class ServerConfig {
    open var mainClassName: String = "org.mjdev.server.MainKt"
    open var servicesDir: String = "services"

//    override fun toMap(): Map<*, *> = toHashMap()

    companion object {
        val configName = "server"
        val configPropertiesFile = "server.prop"
    }
}