package org.mjdev.gradle.commands

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command
import java.io.File

class Dpkg(task: BaseTask) : Command(task) {
    fun createDeb(
        fromDirectory: File,
        outputFile: String
    ) = run(
        command = "dpkg-deb -Z gzip --root-owner-group --build $fromDirectory $outputFile",
        needSudo = false,
        isImportant = false,
        printInfo = false
    )
}