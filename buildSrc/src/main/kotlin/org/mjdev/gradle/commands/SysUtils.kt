package org.mjdev.gradle.commands

import okhttp3.OkHttpClient
import okhttp3.Request
import org.mjdev.gradle.base.BaseTask
import org.mjdev.gradle.base.Command
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("unused", "MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")
class SysUtils(task: BaseTask) : Command(task) {

    val kotlinCompiler = "/opt/kotlinc/bin/kotlinc"
    val kotlinRunner = "/usr/sbin/ktx"

    fun rm(
        what: String,
    ) = commands {
        run("rm -rf $what")
    }

    fun rm(
        what: File,
    ) = commands {
        run("rm -rf ${what.absolutePath}")
    }

    fun download(url: String, dirPath: String) {
        download(url, File(dirPath))
    }

    fun download(url: String, file: File) {
        if (file.isDirectory) {
            file.mkdirs()
        } else {
            file.parentFile.mkdirs()
        }
        OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.MINUTES)
            .build().newCall(Request.Builder().url(url).build()).execute().use { response ->
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
    ) = commands {
        run("/sbin/unzip ${file.absolutePath} -d ${dir.absolutePath}")
    }

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
    ) = commands {
        sudo("chmod a+x ${file.absolutePath}")
    }

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
    ) = commands {
        sudo(
            if (file.isDirectory) "chmod -R $mode ${file.absolutePath}"
            else "chmod $mode ${file.absolutePath}"
        )
    }

    fun sh(block: () -> String) = commands {
        run("sh -c ${block()}")
    }

    fun untar(
        file: File,
        dir: File,
    ) = commands {
        run("tar -xvf ${file.absolutePath} -C ${dir.absolutePath}")
    }

}
