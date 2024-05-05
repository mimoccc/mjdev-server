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
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("reflect"))
    implementation(libs.gradle)
    implementation(libs.gradle.api)
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
