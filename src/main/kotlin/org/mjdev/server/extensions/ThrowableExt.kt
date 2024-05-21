package org.mjdev.server.extensions

import java.io.ByteArrayOutputStream
import java.io.PrintStream

object ThrowableExt {
    fun Throwable.toErrorString(vararg params: Pair<String, Any?>): String {
        val out = ByteArrayOutputStream()
        val print = PrintStream(out)
        print.println("Error: $message")
        params.forEach { p ->
            print.println("${p.first}: ${p.second}")
        }
        printStackTrace(print)
        print.flush()
        print.close()
        return out.toString()
    }
}