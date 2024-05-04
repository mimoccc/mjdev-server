package org.mjdev.gradle.commands

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command

class AptGet(task: BaseTask) : Command(task) {
    fun update() = run(
        command = "apt-get update",
        needSudo = true,
        isImportant = false,
        printInfo = true
    )

    fun upgrade() = run(
        command = "apt-get upgrade",
        needSudo = true,
        isImportant = false,
        printInfo = true
    )

    fun install(pkg: String) = run(
        command = "apt-get install -y $pkg",
        needSudo = true,
        isImportant = true,
        printInfo = true
    )
}
