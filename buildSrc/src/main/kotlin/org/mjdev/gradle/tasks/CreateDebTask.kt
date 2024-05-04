package org.mjdev.gradle.tasks

import org.gradle.api.tasks.Input
import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Architecture
import java.io.File

open class CreateDebTask : BaseTask() {

    @Input
    var packageName = "mjdev-server"

    @Input
    var packageVersion = "1.0.0"

    @Input
    var packageSection = "server"

    @Input
    var packageArchitecture = Architecture.AMD64

    @Input
    var packageMaintainerEmail = "mj@mjdev.org"

    @Input
    var packageMaintainerName = "mjdev"

    @Input
    var packageDescription = "mjdev server package."

    @Input
    var packageFullDescription = createString {
        "Server package that installs complete mjdev server," +
            "which include all the needs to run small it company."
    }

    @Input
    var packageDependencies = mutableListOf(
        "zip",
        "unzip",
        "fail2ban",
        "ufw",
        "certbot",
        "nginx-full",
        "nodejs",
        "npm",
        "openjdk-17-jre",
        "openjdk-17-jdk",
        "gradle",
    )

    @Input
    var outputFileName = "$packageName.deb"

    private val outputFile
        get() = rootDir.resolve(outputFileName)
    private val tempDebDirectory
        get() = buildDir.resolve("deb")
    private val debianDirectory
        get() = tempDebDirectory.resolve("DEBIAN")
    private val controlFile
        get() = debianDirectory.resolve("control")
    private val packageDependenciesString
        get() = packageDependencies.joinToString(",")
    private val packageMaintainer
        get() = "$packageMaintainerName <$packageMaintainerEmail>"
    private val controlFileContent
        get() = createString {
            "Package: $packageName\n" +
                "Version: $packageVersion\n" +
                "Section: $packageSection\n" +
                "Priority: optional\n" +
                "Architecture: ${packageArchitecture.value}\n" +
                "Depends: $packageDependenciesString\n" +
                "Maintainer: $packageMaintainer\n" +
                "Description: $packageDescription\n" +
                " " +packageFullDescription + "\n" +
                "Homepage: https://mjdev.org\n" +
                "Package-Type: deb\n"
        }
    private val excludeFiles = listOf(
        controlFile.absolutePath,
        outputFile.absolutePath,
        rootDir.resolve(".git").absolutePath,
        rootDir.resolve(".gradle").absolutePath,
        rootDir.resolve("build").absolutePath,
        rootDir.resolve("buildSrc").resolve(".gradle").absolutePath,
        rootDir.resolve("buildSrc").resolve("build").absolutePath,
        rootDir.resolve("local.properties").absolutePath
    )
    private val packageDir = tempDebDirectory.resolve("mjdev")
    private val installFile = packageDir.resolve("install.sh")
    private val postInstallFile = debianDirectory.resolve("postinst")
    private val postInstallFileContent = createString {
        "#!/bin/bash\n" +
            "cd /${installFile.parentFile.name}\n" +
            "chmod a+x ${installFile.name}\n" +
            "chmod a+x gradlew\n" +
            "sh -f ${installFile.name}\n"
    }

    @Input
    var packageFileMap = mutableMapOf<String, String>(
        rootDir.absolutePath to packageDir.absolutePath
    )

    private fun copyRecursively(from: File, to: File) {
        if (excludeFiles.contains(from.absolutePath)) {
            return
        } else if (from.isDirectory) {
            to.mkdirs()
            from.listFiles()?.forEach { what ->
                copyRecursively(what, to.resolve(what.name))
            }
        } else {
            to.parentFile.mkdirs()
            from.copyTo(to, true)
        }
    }

    override fun init() {
        group = "mjdev"
        description = "Create deb file from root directory."
        finalizedBy("clean")
    }

    override fun start() {
        outputFile.delete()
        tempDebDirectory.deleteRecursively()
        tempDebDirectory.mkdirs()
        debianDirectory.mkdirs()
        controlFile.writeText(controlFileContent)
        packageFileMap.forEach { (source, destination) ->
            val sourceFile = rootDir.resolve(source)
            val destinationFile = rootDir.resolve(destination)
            copyRecursively(sourceFile, destinationFile)
        }
        postInstallFile.writeText(postInstallFileContent)
        sysCmd.makeExecutable(tempDebDirectory.resolve("mjdev").resolve("gradlew"))
        sysCmd.makeExecutable(tempDebDirectory.resolve("mjdev").resolve("install.sh"))
        sysCmd.makeExecutable(tempDebDirectory.resolve("mjdev").resolve("createDeb.sh"))
        sysCmd.chmod(postInstallFile, 755)
        dpkgCmd.createDeb(tempDebDirectory, outputFileName)
        sysCmd.chmod(outputFile, 666)

    }

    override fun finish() {
        try {
            tempDebDirectory.deleteRecursively()
        } catch (t: Throwable) {
            print("Failed to delete temporry directory.")
        }
    }
}