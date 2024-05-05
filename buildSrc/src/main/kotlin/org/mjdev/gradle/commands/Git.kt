package org.mjdev.gradle.commands

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command
import java.io.File

@Suppress("unused")
class Git(task: BaseTask) : Command(task) {
    fun clone(
        url: String,
        dir: File
    ) = run(
        command = "git clone $url ${dir.absolutePath}",
        needSudo = false,
        isImportant = false,
        printInfo = false
    )
}