package org.mjdev.gradle.commands

import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command
import java.io.File

@Suppress("unused")
class NodeJs(task: BaseTask) : Command(task) {
    private val File.isExecutable: Boolean
        get() = canExecute()

    private val nodejsDownloadUrl: String =
        "https://nodejs.org/dist/v20.12.2/node-v20.12.2-linux-x64.tar.xz"

    private val nodeDir
        get() = File(nodejsDownloadUrl).name
            .replace(".tar.xz", "")
            .let { dn ->
                task.gradleDir
                    .resolve("nodejs")
                    .resolve(dn)
            }

    private val nodeBinDir
        get() = nodeDir.resolve("bin")

    private val node
        get() = nodeBinDir.resolve("node")

    private val npm
        get() = nodeBinDir.resolve("npm")

    private val npx
        get() = nodeBinDir.resolve("npx")

    private val corepack
        get() = nodeBinDir.resolve("corepack")

    private val nodejsDir
        get() = task.gradleDir.resolve("nodejs")

    private val nodeJsTempFile
        get() = nodejsDir.resolve("node.zip")

    private val nodeJsInstalled
        get() = node.let {
            it.exists() && it.isExecutable
        }

    private fun checkInstalled() {
        val sysCmd = task.sysCmd
        if (!nodeJsInstalled) {
            nodejsDir.mkdirs()
            sysCmd.download(nodejsDownloadUrl, nodeJsTempFile)
            sysCmd.untar(nodeJsTempFile, nodejsDir)
        }
    }

    fun startService(serviceDir: File) {
        checkInstalled()
        commands {
            path(node)
            path(npm)
            path(npx)
            path(corepack)
            workingDir = serviceDir
            run(npm, "install")
            run(npm, true,"start")
        }
    }
}