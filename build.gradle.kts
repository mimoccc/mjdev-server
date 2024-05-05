repositories {
    mavenCentral()
    google()
}

plugins {
    id("ServerPlugin")
}

server {
    mainClassName = "org.mjdev.server.MainKt"
    servicesDir = "services"
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.gradle.api)
        classpath(libs.gradle.kotlin.plugin)
        classpath(libs.gradle.ktor.plugin)
    }
}
