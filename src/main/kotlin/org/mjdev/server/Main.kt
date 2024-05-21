package org.mjdev.server

import org.mjdev.server.server.Server
import kotlin.system.exitProcess

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) {
    var server: Server? = null
    try {
        server = Server()
        server.start()
    } catch (error: Throwable) {
        try {
            server?.stop()
        } catch (_: Throwable) {
            // no op
        }
        error.printStackTrace()
        exitProcess(1)
    }
}
