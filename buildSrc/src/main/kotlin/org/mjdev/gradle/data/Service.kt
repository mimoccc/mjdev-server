package org.mjdev.gradle.data

import java.io.File

class Service(
    private val file: File
) {
    private val isNodeJs : Boolean
        get() = file.resolve("package.json").exists()
    private val isKts : Boolean
        get() = file.resolve("build.gradle.kts").exists()
    private val isStatic : Boolean
        get() = (!isNodeJs) && (!isKts) && file.resolve("index.html").exists()

}