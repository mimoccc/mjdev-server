@file:Suppress("unused")

package org.mjdev.server.plugins

import org.mjdev.server.dynu.UpdateService
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.util.*

@KtorDsl
class DynDnsPluginConfig {
    var user: String = ""
    var password: String = ""
    var ttl: Int = 120
}

val DynDnsPlugin: ApplicationPlugin<DynDnsPluginConfig> = createApplicationPlugin(
    name = "DynDnsPlugin",
    ::DynDnsPluginConfig
) {
    val dynuService by lazy { UpdateService() }
    on(MonitoringEvent(ApplicationStarted)) { application ->
        application.log.info("Updating dynamic dns ip address.")
        dynuService.apply {
            user = pluginConfig.user
            password = pluginConfig.password
            ttl = pluginConfig.ttl
            startService()
        }
    }
    on(MonitoringEvent(ApplicationStopped)) { application ->
        dynuService.stopService()
        application.environment.monitor.unsubscribe(ApplicationStarted) {}
        application.environment.monitor.unsubscribe(ApplicationStopped) {}
    }
}