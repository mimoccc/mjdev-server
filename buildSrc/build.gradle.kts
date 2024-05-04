import groovy.lang.Closure

/*
 *  Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    groovy
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("reflect"))
    implementation(libs.gradle)
    implementation(libs.gradle.api)
    implementation(libs.gradle.kotlin.plugin)
    implementation(libs.gradle.kotlin.stdlib.jdk8)
    implementation(libs.okhttp3)
}

gradlePlugin {
    plugins {
        register("ServerPlugin") {
            id = "ServerPlugin"
            displayName = "ServerPlugin"
            description = ""
            implementationClass = "org.mjdev.gradle.plugin.ServerPlugin"
        }
    }
}

kotlin {
    val hostOs = System.getProperty("os.name")
    if (hostOs.startsWith("linux")) {
        throw (GradleException("This package is only for linux machines."))
    }
}