package org.mjdev.server.plugins

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.plugins.origin

object SimplePluginFactory {
    val SimplePlugin = createApplicationPlugin(
        name = "SimplePlugin"
    ) {
        onCall { call ->
            call.request.origin.apply {
                println("Request URL: $scheme://$localHost:$localPort$uri")
            }
        }
    }
}