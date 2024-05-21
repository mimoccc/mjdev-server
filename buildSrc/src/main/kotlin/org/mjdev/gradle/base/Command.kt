package org.mjdev.gradle.base

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File

open class Command(
    val task: BaseTask
) {
    val project
        get() = task.project

    fun commands(scope: CommandScope.() -> Unit) {
        val cmdScope = CommandScope(this)
        scope(cmdScope)
        with(cmdScope) {
            path(rootDir)
            path(buildDir)
            allCommands.forEach { command ->
                val commandOutput = ByteArrayOutputStream()
                println("Executing command: ${command.command}")
                println("Arguments: [${command.args.joinToString(", ")}]")
                try {
                    val pb = ProcessBuilder(mutableListOf(command.command).apply {
                        addAll(command.args)
                    })
                    pb.directory(cmdScope.workingDir)
                    pb.environment().putAll(cmdScope.environment.map { it.key to it.value.toString() })
                    // todo
//                    pb.redirectOutput(StreamRedirect(commandOutput))
//                    pb.redirectErrorStream(true)
                    val p = pb.start()
                    if(!command.isDaemon) {
                        p.waitFor()
                    }
                    onResult(p.exitValue(), commandOutput.toString(), null)
                } catch (e: Throwable) {
                    onResult(-1, commandOutput.toString(), e)
                }
            }
        }
    }

    class CommandScope(
        private val cmd: Command,
        val project: Project = cmd.project
    ) {
        private val environments =
            mutableMapOf<String, Any?>("PATH" to "\"\$PATH:/bin:/sbin:/usr/bin/:/usr/sbin:/usr/local/bin:/usr/local/sbin\"")
        private val commands = mutableListOf<RunCommand>()

        val rootDir
            get() = project.rootDir

        @Suppress("DEPRECATION")
        val buildDir
            get() = project.buildDir
        val isSudo: Boolean
            get() = System.getProperty("user.name") == "root"
        val environment
            get() = environments

        val allCommands
            get() = commands

        var workingDir = project.rootDir
//        var ignoreExitValue = false
        var onResult: (code: Int, output: String, error: Throwable?) -> Unit = { c, o, e ->
            println("Command exit value: $c")
            println("Command output: $o")
            if (e != null) {
                println("Error: ${errorString(e)}")
            }
        }

        private fun errorString(e: Throwable): String = mutableListOf<String>().apply {
            var ee: Throwable? = e
            while (ee != null) {
                add(ee.message ?: "Unknown error")
                ee = ee.cause as? Exception
            }
        }.joinToString("\n").plus(".")

        fun path(file: File) {
            if (file.isDirectory) path(file.absolutePath)
            else path(file.parentFile.absolutePath)
        }

        fun path(path: String) {
            val oldPath = env("PATH")
            if (oldPath.contains(path)) return
            val newPath = if (oldPath.isNotEmpty()) {
                "\"\$PATH:${oldPath.replace("\"\$PATH:", "").replace("\"", "")}:$path\""
            } else {
                "\"\$PATH:$path\""
            }
            env("PATH", newPath)
        }

        fun env(key: String, block: (String) -> Unit) {
            block(environments[key]?.toString() ?: "")
        }

        fun env(key: String): String = environments[key]?.toString() ?: ""

        fun env(key: String, value: String) {
            environments[key] = value
        }

        fun run(command: String) {
            this.commands.add(RunCommand(command, false))
        }

        fun run(command: String, isDaemon: Boolean = false) {
            this.commands.add(RunCommand(command, isDaemon))
        }

        fun run(command: String, isDaemon: Boolean, vararg args: String) {
            this.commands.add(RunCommand(command, isDaemon, args))
        }

        fun run(command: File, vararg args: String) =
            run(command.absolutePath, false, *args)

        fun run(command: File, isDaemon: Boolean = false, vararg args: String) =
            run(command.absolutePath, isDaemon, *args)

        fun sudo(command: String, isDaemon: Boolean = false) {
            run(command.let { c -> "sudo $c" }, isDaemon)
        }

    }

    class RunCommand(
        val command: String,
        val isDaemon: Boolean = false,
        val args: Array<out String>
    ) {
        constructor(cmd: String, isDaemon: Boolean = false) : this(
            cmd.split(" ").first(),
            isDaemon,
            cmd.split(" ")
                .toMutableList().apply { removeAt(0) }
                .toTypedArray()
        )
    }

}
