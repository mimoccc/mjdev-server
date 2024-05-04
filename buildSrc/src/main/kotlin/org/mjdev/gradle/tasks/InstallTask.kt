package org.mjdev.gradle.tasks

import org.mjdev.gradle.base.BaseTask

open class InstallTask : BaseTask() {

    private val ktxCmdFileName1 = "/usr/sbin/ktx"
    private val ktxCmdFileName2 = "/usr/bin/ktx"
    private val kotlinInstallDir = "/opt/"
    private val kotlinInstalledDir = kotlinInstallDir + "kotlinc"
    private val kotlinCompilerTempFileName = "kotlin-compiler.zip"
    private val testKtxFileName = "test.kts"
    private val testJarFileName = "test.jar"

    private val kotlinInstallPackageUrl =
        "https://github.com/JetBrains/kotlin/releases/download/v1.9.23/kotlin-compiler-1.9.23.zip"

    private val ktxCmdFileContent = "#!/bin/sh\n" +
                "$kotlinInstalledDir/bin/kotlinc -script ${'$'}1\n" +
                "#"

    private val kotlinCompilerTempFile
        get() = tmpDir.resolve(kotlinCompilerTempFileName)

    private val testKtxFile
        get() = scriptsDir.resolve(testKtxFileName)

    private val testJarFile
        get() = tmpDir.resolve(testJarFileName)

    init {
        group = "mjdev"
        description = "Installs the server"
    }

    override fun init() {
        group = "mjdev"
        description = "Install server and components."
        finalizedBy("clean")
    }

    override fun start() {
        println("mjdev $name server called")
        println("installing kotlin native...")
        sysCmd.rm(kotlinInstalledDir)
        sysCmd.download(
            kotlinInstallPackageUrl,
            kotlinCompilerTempFile
        )
        sysCmd.unzip(kotlinCompilerTempFile, kotlinInstallDir)
        sysCmd.createFile(ktxCmdFileName1) {
            ktxCmdFileContent
        }
        sysCmd.createFile(ktxCmdFileName2) {
            ktxCmdFileContent
        }
        sysCmd.chmod(ktxCmdFileName1, 777)
        sysCmd.chmod(ktxCmdFileName2, 777)
        sysCmd.makeExecutable(ktxCmdFileName1)
        sysCmd.makeExecutable(ktxCmdFileName2)
//        println("test compile ktx file")
//        sysCmd.ktxCompile(testKtxFile, testJarFile)
//        println("test start compiled ktx file")
//        sysCmd.startJava(testJarFile)
        println("test start non-compiled ktx file...")
        sysCmd.ktx(testKtxFile)
        println("done.")
    }

    override fun finish() {
    }
}