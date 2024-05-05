package org.mjdev.gradle.base

import org.mjdev.gradle.exceptions.CommandException
import org.mjdev.gradle.exceptions.FatalCommandException
import java.io.File
import kotlin.jvm.Throws

@Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")
open class Command(
    val task: BaseTask
) {
    val project
        get() = task.project

    val rootDir
        get() = task.rootDir

    val buildDir
        get() = task.buildDir

    private val isSudo: Boolean
        get() {
            val user = System.getProperty("user.name")
            return user == "root"
        }

    private val commandStack = mutableListOf<CommandStackEntry>()

    fun run(
        command: String,
        needSudo: Boolean = false,
        printInfo: Boolean = true,
        isImportant: Boolean = true,
        checkCommand: (c: String) -> Boolean = { c ->
            runCatching { File(c).exists() }.getOrDefault(false)
        },
        onFailure: (e: CommandException) -> Unit = { e ->
            if (e.isFatal && isImportant) {
                task.printlnErr("Command failed: $command, ${e.message}")
                throw (e)
            } else {
                task.println("Command failed: $command, ${e.message}")
            }
        },
        onResult: (command: CommandStackEntry) -> Unit = { cmd ->
            if (printInfo) {
                val out = cmd.output.toString()
                val err = cmd.errorOutput.toString()
                task.println("Command result: $command")
                if (out.isNotEmpty()) {
                    task.println("Output: $out")
                }
                if (err.isNotEmpty()) {
                    task.println("Error: $err")
                }
            }
        }
    ) {
        var cmd = command
        try {
            if (printInfo) task.println("Running command: $command")
            if (!checkCommand(cmd.split(" ").first())) {
                cmd = "/usr/bin/$cmd"
                if (!checkCommand(cmd.split(" ").first())) {
                    cmd = "/usr/sbin/$cmd"
                    if (!checkCommand(cmd.split(" ").first())) {
                        throw (CommandException("Command not exists: $command"))
                    }
                }
            }
            commandStack.add(CommandStackEntry(cmd, onFailure, needSudo))
            val iterator = commandStack.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                iterator.remove()
                _run(entry, onResult)
            }
        } catch (t: Throwable) {
            val e = if (t is CommandException) t
            else CommandException(t)
            onFailure(e)
        }
    }

    @Throws(CommandException::class)
    private fun _run(
        command: CommandStackEntry,
        onResult: (command: CommandStackEntry) -> Unit
    ) {
        try {
            if (command.needSudo) {
                if (!isSudo) {
                    throw (FatalCommandException("You need to be root to run this command"))
                }
            }
            project.exec {
                workingDir = project.rootDir
                commandLine = listOf("sh", "-c", command.command)
                isIgnoreExitValue = true
                standardOutput = command.output
                errorOutput = command.errorOutput
            }.let { result ->
                if (result.exitValue != 0) {
                    throw (FatalCommandException(
                        "Command failed: ${command.command}",
                        CommandException(
                            "Command exited with status code: ${result.exitValue}\n" +
                                command.errorOutput.toString()
                        )
                    ))
                }
                onResult(command)
            }
        } catch (e: Exception) {
            throw (when (e) {
                is CommandException -> e
                else -> CommandException(e)
            })
        }
    }

    fun createString(block: () -> String): String =
        task.createString(block)

}
