package org.mjdev.gradle.base

import org.gradle.StartParameter
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.mjdev.gradle.commands.AptGet
import org.mjdev.gradle.commands.Dpkg
import org.mjdev.gradle.commands.Git
import org.mjdev.gradle.commands.HostCmd
import org.mjdev.gradle.commands.SysUtils
import org.mjdev.gradle.commands.NodeJs
import org.mjdev.gradle.data.ServiceFile
import org.mjdev.gradle.extensions.ProjectExt.extension
import org.mjdev.gradle.extensions.ProjectExt.runningFromAndroidStudio
import org.mjdev.gradle.plugin.config.ServerConfig
import java.io.File
import org.mjdev.gradle.extensions.ProjectExt.task

@Suppress("LeakingThis", "unused", "MemberVisibilityCanBePrivate")
abstract class BaseTask : DefaultTask() {
    @get:Internal
    val debug: Boolean
        get() = project.runningFromAndroidStudio

    @get:Internal
    val gradleDir
        get() = project.rootDir.resolve("gradle")

//    @get:Internal
//    val aptGet by lazy { AptGet(this) }

    @get:Internal
    val sysCmd by lazy { SysUtils(this) }

    @get:Internal
    val dpkgCmd by lazy { Dpkg(this) }

    @get:Internal
    val gitCmd by lazy { Git(this) }

    @get:Internal
    val hostsCmd by lazy { HostCmd(this) }

    @get:Internal
    val nodeCmd by lazy { NodeJs(this) }

    @get:Internal
    val isDebug
        get() = project.runningFromAndroidStudio

    @get:Internal
    val log
        get() = this.logger

    @get:Internal
    val rootDir: File
        get() = project.rootDir

    @Suppress("DEPRECATION")
    @get:Internal
    val buildDir: File
        get() = project.buildDir

    @get:Internal
    val buildSrcDir: File
        get() = project.rootDir.resolve("buildSrc")

    @get:Internal
    val tmpDir
        get() = buildDir.resolve("tmp")

    @get:Internal
    val scriptsDir
        get() = rootDir.resolve("scripts")

    @get:Internal
    val gradleParams: StartParameter
        get() = project.gradle.startParameter

    @get:Internal
    val taskParameters: Map<String, String>
        get() = gradleParams.projectProperties

    @get:Internal
    val runningTasks: List<String>
        get() = gradleParams.taskNames

    @get:Internal
    val hasAssembleTask: Boolean
        get() = runningTasks.any { task -> task.lowercase().contains("assemble") }

    @get:Internal
    val isAssembleTask
        get() = hasAssembleTask

    @get:Internal
    val tasks: TaskContainer
        get() = project.tasks

    @get:Internal
    val serverConfig
        get() = project.extension<ServerConfig>()

    init {
        group = "mjdev"
        if (debug) println("Initialising task $name [${this::class.java.simpleName}]...")
        init()
    }

    @TaskAction
    fun taskAction() {
        if (debug) println("Starting task $name [${this::class.java.simpleName}]...")
        start()
//        when (isAssembleTask) {
//            true -> onAssemble()
//            false -> onClean()
//        }
        if (debug) println("finishing task $name [${this::class.java.simpleName}]...")
        finish()
    }

    abstract fun init()

    abstract fun start()

    abstract fun finish()

    open fun onClean() {}

    open fun onAssemble() {}

    fun println(message: String) {
        log.lifecycle(message)
    }

    fun printlnErr(s: String) {
        log.error(s)
    }

    fun printlnErr(t: Throwable) {
        log.error(t.message)
    }

    fun createString(block: () -> String): String = StringBuilder().apply {
        append(block())
    }.toString()

    fun parseServices(
        services: File
    ) = runCatching {
        services.listFiles()?.toList()?.mapNotNull { file ->
            if (file.name.endsWith(".service")) ServiceFile(file) else null
        }?.filter { f -> f.isEnabled } ?: emptyList()
    }.onFailure { error ->
        println("Error parsing services: $error")
    }.getOrDefault(emptyList())

    inline fun <reified T : Task> Task.task(scoped: T.() -> Unit = {}): T  =
        project.task(scoped)

}