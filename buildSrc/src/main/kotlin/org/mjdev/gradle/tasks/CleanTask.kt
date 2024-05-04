package org.mjdev.gradle.tasks

import org.mjdev.gradle.base.BaseTask

open class CleanTask : BaseTask() {

    private val clearFiles = listOf(
        rootDir.resolve("build"),
        rootDir.resolve(".gradle"),
        buildDir.resolve("build"),
        buildDir.resolve(".gradle"),
        buildSrcDir.resolve("build"),
        buildSrcDir.resolve(".gradle"),
    )

    override fun init() {
        group = "mjdev"
        description = "Clean workspace."
    }

    override fun start() {
        clearFiles.forEach { file ->
            if (file.exists()) {
                file.deleteRecursively()
            }
        }
    }

    override fun finish() {
    }
}