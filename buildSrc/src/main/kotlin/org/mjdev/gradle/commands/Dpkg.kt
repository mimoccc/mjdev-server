package org.mjdev.gradle.commands

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command
import java.io.File

@Suppress("unused")
class Dpkg(task: BaseTask) : Command(task) {
    fun createDeb(
        fromDirectory: File,
        outputFile: String
    ) = commands {
        run("dpkg-deb -Z gzip --root-owner-group --build $fromDirectory $outputFile")
    }
}