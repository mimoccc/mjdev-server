package org.mjdev.gradle.commands

import okhttp3.OkHttpClient
import okhttp3.Request
import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
class SysUtils(task: BaseTask) : Command(task) {

    val kotlinCompiler = "/opt/kotlinc/bin/kotlinc"
    val kotlinRunner = "/usr/sbin/ktx"

    fun rm(
        what: String,
        needSudo: Boolean = true,
        isImportant: Boolean = false,
        printInfo: Boolean = false
    ) = run(
        command = "rm -rf $what",
        needSudo = needSudo,
        isImportant = isImportant,
        printInfo = printInfo
    )

    fun download(url: String, dirPath: String) {
        download(url, File(dirPath))
    }

    fun download(url: String, file: File) {
        if (file.isDirectory) {
            file.mkdirs()
        } else {
            file.parentFile.mkdirs()
        }
        OkHttpClient().newCall(Request.Builder().url(url).build()).execute().use { response ->
            file.outputStream().use {
                it.write(response.body.bytes())
            }
        }
    }

    fun unzip(
        filePath: String,
        dirPath: String,
        needSudo: Boolean = true,
        isImportant: Boolean = true,
        printInfo: Boolean = false
    ) = unzip(File(filePath), File(dirPath), needSudo, isImportant, printInfo)

    fun unzip(
        filePath: String,
        dir: File,
        needSudo: Boolean = true,
        isImportant: Boolean = true,
        printInfo: Boolean = false
    ) = unzip(File(filePath), dir, needSudo, isImportant, printInfo)

    fun unzip(
        file: File,
        dirPath: String,
        needSudo: Boolean = true,
        isImportant: Boolean = true,
        printInfo: Boolean = false
    ) = unzip(file, File(dirPath), needSudo, isImportant, printInfo)

    fun unzip(
        file: File,
        dir: File,
        needSudo: Boolean = true,
        isImportant: Boolean = true,
        printInfo: Boolean = false
    ) = run(
        command = "unzip ${file.absolutePath} -d ${dir.absolutePath}",
        needSudo = needSudo,
        isImportant = isImportant,
        printInfo = printInfo
    )

    fun createFile(filePath: String, what: () -> String) {
        createFile(File(filePath), what)
    }

    fun createFile(file: File, what: () -> String) {
        file.writeText(what())
    }

    fun makeExecutable(filePath: String) {
        makeExecutable(File(filePath))
    }

    fun makeExecutable(
        file: File,
        isImportant: Boolean = true,
        printInfo: Boolean = false
    ) = run(
        command = "chmod a+x ${file.absolutePath}",
        needSudo = true,
        isImportant = isImportant,
        printInfo = printInfo
    )

//    fun ktxCompile(testKtxFile: File, outFile: File) = run(
//        command = "$kotlinCompiler ${testKtxFile.absolutePath} -include-runtime -d ${outFile.absolutePath}",
//        needSudo = true,
//        isImportant = false,
//        printInfo = false
//    )

//    fun startJava(jarFile: File) = run(
//        command = "java -jar ${jarFile.absolutePath}",
//        needSudo = true,
//        isImportant = false,
//        printInfo = false
//    )

    @Suppress("UNUSED_PARAMETER")
    fun ktx(
        ktxFile: File,
        onOutput: () -> Unit = {}
    ) = run(
        command = "$kotlinRunner ${ktxFile.absolutePath}",
        needSudo = false,
        isImportant = false,
        printInfo = true
    )

    fun chmod(
        filePath: String,
        mode: Int,
        isImportant: Boolean = true,
        printInfo: Boolean = false
    ) = chmod(File(filePath), mode, isImportant, printInfo)

    fun chmod(
        file: File,
        mode: Int,
        isImportant: Boolean = true,
        printInfo: Boolean = false
    ) = run(
        command = if (file.isDirectory) "chmod -R $mode ${file.absolutePath}"
        else "chmod $mode ${file.absolutePath}",
        needSudo = true,
        isImportant = isImportant,
        printInfo = printInfo
    )

    fun sh(block: () -> String) = run(
        command = block(),
        needSudo = true,
        isImportant = true,
        printInfo = true
    )

}
