package org.mjdev.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.mjdev.gradle.commands.AptGet
import org.mjdev.gradle.commands.SysUtils
import org.mjdev.gradle.commands.Dpkg
import org.mjdev.gradle.commands.Git
import java.io.File

@Suppress("LeakingThis", "unused")
abstract class BaseTask : DefaultTask() {
    @Input
    var debug: Boolean = false

    @get:Internal
    val aptGet by lazy { AptGet(this) }

    @get:Internal
    val sysCmd by lazy { SysUtils(this) }

    @get:Internal
    val dpkgCmd by lazy { Dpkg(this) }

    @get:Internal
    val gitCmd by lazy { Git(this) }

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

    init {
        group = "mjdev"
        if (debug) println("Initialising task $name [${this::class.java.simpleName}]...")
        init()
    }

    @TaskAction
    fun taskAction() {
        if (debug) println("Starting task $name [${this::class.java.simpleName}]...")
        start()
        if (debug) println("finishing task $name [${this::class.java.simpleName}]...")
        finish()
    }

    abstract fun init()

    abstract fun start()

    abstract fun finish()

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
}