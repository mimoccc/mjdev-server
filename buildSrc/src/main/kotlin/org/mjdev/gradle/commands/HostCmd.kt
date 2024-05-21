package org.mjdev.gradle.commands

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command
import java.io.File

//todo validation !!!
@Suppress("unused", "MemberVisibilityCanBePrivate")
class HostCmd(task: BaseTask) : Command(task) {

    private val hostNameFile
        get() = File("/etc/hostname")

    val hostName
        get() = hostNameFile.readText().trim()

    private val localHostFile
        get() = File("/etc/hosts")

    val hosts
        get() = localHostFile.readLines()
            .removeEmptyLines()
            .map { line -> Host(line) }
            .toMutableList()

    fun contains(host: String) =
        hosts.any { it.host == host }

    fun add(host: String, ip: String) {
        if (!contains(host)) {
            hosts.add(Host(host, ip))
            save()
        }
    }

    fun remove(host: String) {
        if (contains(host)) {
            hosts.removeIf { it.host == host }
            save()
        }
    }

    fun save() {
        StringBuilder().apply {
            hosts.forEach { host ->
                if (host.isNotEmpty) {
                    appendLine(host.toString())
                }
            }
        }.toString().let { text ->
            localHostFile.writeText(text)
        }
    }

    class Host(
        private val line: String,
        private val parts: List<String> = line.trim().split(" ")
    ) {
        constructor(host: String, ip: String) : this("$ip, $host")

        val ip
            get() = if (parts.isNotEmpty()) parts[0].trim() else ""
        val host
            get() = if (parts.size > 1) parts[1].trim() else ""

        val isEmpty
            get() = line.trim().isEmpty()

        val isNotEmpty
            get() = !isEmpty

        val isComment
            get() = parts.isNotEmpty() && parts[0].startsWith("#")

        override fun toString(): String = when {
            isEmpty -> ""
            isComment -> line
            else -> "$ip $host"
        }
    }

    fun List<String>.removeEmptyLines(): List<String> = mapNotNull { text ->
        if (text.trim().isBlank()) null else text
    }
}


