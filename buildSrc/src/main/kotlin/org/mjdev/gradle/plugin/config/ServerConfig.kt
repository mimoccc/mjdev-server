package org.mjdev.gradle.plugin.config

open class ServerConfig : IConfig {
    open var applicationName: String = "mjdev-server"
    open var mainClassName: String = "org.mjdev.server.MainKt"
    open var servicesDir: String = "services"


    override fun toMap(): Map<*, *> = toHashMap<ServerConfig>()

    companion object {
        const val configName = "server"
        const val configPropertiesFile = "server.prop"
    }
}