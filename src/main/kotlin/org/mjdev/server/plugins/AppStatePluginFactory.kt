package org.mjdev.server.plugins

import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.util.*
import io.ktor.events.EventDefinition
import io.ktor.http.HttpStatusCode

object AppStatePluginFactory {
    private val NotFoundEvent: EventDefinition<ApplicationCall> = EventDefinition()

    @Suppress("CustomizableKDocMissingDocumentation", "unused")
    @KtorDsl
    class AppStatePluginConfig {
        var onServerStart: (application: Application) -> Unit = {}
        var onServerStop: (application: Application) -> Unit = {}
    }

    val AppStatePlugin: ApplicationPlugin<AppStatePluginConfig> = createApplicationPlugin(
        name = "AppStatePlugin",
        ::AppStatePluginConfig
    ) {
        on(MonitoringEvent(ApplicationStarted)) { application ->
            application.log.info("Server is started")
            pluginConfig.onServerStart(application)
        }
        on(MonitoringEvent(ApplicationStopped)) { application ->
            application.log.info("Server is stopped")
            pluginConfig.onServerStop(application)
            application.environment.monitor.unsubscribe(ApplicationStarted) {}
            application.environment.monitor.unsubscribe(ApplicationStopped) {}
        }
        on(ResponseSent) { call ->
            if (call.response.status() == HttpStatusCode.NotFound) {
                this@createApplicationPlugin
                    .application
                    .environment
                    .monitor.raise(NotFoundEvent, call)
            }
        }
    }
}
